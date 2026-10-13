import java.time.Duration;
import java.time.LocalDateTime;

/** Самопроверка правил интервалов. */
public class SelfCheck {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 9, 0);
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        BookingRepository repo = new BookingRepository();
        BookingService s = new BookingService(repo);
        Booking base = s.book("R1", t(10, 0), t(11, 0), NOW);
        check("типовое бронирование 10:00-11:00, длительность 60 мин", base.duration().toMinutes() == 60);

        // пересечения
        check("стык 11:00-12:00 — не конфликт", s.isAvailable("R1", t(11, 0), t(12, 0)));
        check("стык 09:00-10:00 — не конфликт", s.isAvailable("R1", t(9, 0), t(10, 0)));
        check("частичное слева 09:30-10:01 — конфликт", !s.isAvailable("R1", t(9, 30), t(10, 1)));
        check("частичное справа 10:59-11:30 — конфликт", !s.isAvailable("R1", t(10, 59), t(11, 30)));
        check("внутри 10:15-10:45 — конфликт", !s.isAvailable("R1", t(10, 15), t(10, 45)));
        check("охватывает 09:00-12:00 — конфликт", !s.isAvailable("R1", t(9, 0), t(12, 0)));
        check("совпадает 10:00-11:00 — конфликт", !s.isAvailable("R1", t(10, 0), t(11, 0)));
        check("другая комната — не конфликт", s.isAvailable("R2", t(10, 0), t(11, 0)));
        int before = repo.count();
        s.isAvailable("R1", t(12, 0), t(13, 0));
        check("isAvailable не меняет репозиторий", repo.count() == before);
        try {
            s.book("R1", t(10, 30), t(11, 30), NOW);
            check("book с конфликтом -> исключение", false);
        } catch (BookingConflictException e) {
            check("конфликт возвращает id бронирования " + e.getConflictingBookingId(),
                    e.getConflictingBookingId().equals(base.getId()));
        }

        // длительность: 15 мин .. 10 ч (вариант 1)
        expect("14 минут", () -> s.book("R3", t(12, 0), t(12, 14), NOW));
        check("ровно 15 минут", run(() -> s.book("R3", t(12, 0), t(12, 15), NOW)));
        check("ровно 10 часов", run(() -> s.book("R4", t(9, 0), t(19, 0), NOW)));
        expect("10 часов 1 минута", () -> s.book("R5", t(9, 0), t(19, 1), NOW));
        expect("окончание = начало", () -> s.book("R6", t(12, 0), t(12, 0), NOW));
        expect("окончание раньше начала", () -> s.book("R6", t(12, 0), t(11, 0), NOW));
        expect("начало в прошлом (08:59)", () -> s.book("R6", t(8, 59), t(9, 30), NOW));
        check("начало ровно сейчас допустимо", run(() -> s.book("R7", NOW, NOW.plusMinutes(30), NOW)));
        expect("null вместо времени", () -> s.book("R6", null, t(11, 0), NOW));
        expect("неверный формат времени", () -> BookingApp.parse("20.10.2026 10:00"));

        // переход через полночь
        LocalDateTime lateStart = LocalDateTime.of(2026, 10, 20, 23, 0);
        LocalDateTime nextDay = LocalDateTime.of(2026, 10, 21, 1, 0);
        check("23:00-01:00 следующего дня допустимо (120 мин)",
                run(() -> s.book("R8", lateStart, nextDay, NOW)));
        check("00:30-02:00 следующего дня конфликтует с ночным",
                !s.isAvailable("R8", nextDay.minusMinutes(30), nextDay.plusHours(1)));
        check("тот же час другого дня — не конфликт (сравниваются даты)",
                s.isAvailable("R1", t(10, 0).plusDays(1), t(11, 0).plusDays(1)));

        // буфер (индивидуальная настройка): 10 минут
        BookingService buffered = new BookingService(new BookingRepository(), Duration.ofMinutes(10));
        buffered.book("R1", t(10, 0), t(11, 0), NOW);
        check("буфер 10 мин: 11:05 — конфликт", !buffered.isAvailable("R1", t(11, 5), t(12, 0)));
        check("буфер 10 мин: 11:10 — допустимо", buffered.isAvailable("R1", t(11, 10), t(12, 0)));
        check("буфер 10 мин: окончание 09:50 — допустимо", buffered.isAvailable("R1", t(9, 0), t(9, 50)));

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static LocalDateTime t(int hour, int minute) {
        return LocalDateTime.of(2026, 10, 20, hour, minute);
    }

    private static boolean run(Runnable action) {
        try {
            action.run();
            return true;
        } catch (RuntimeException e) {
            System.out.println("       неожиданно: " + e.getMessage());
            return false;
        }
    }

    private static void expect(String name, Runnable action) {
        try {
            action.run();
            check(name + " -> ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check(name + " -> " + e.getMessage(), true);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
    }
}
