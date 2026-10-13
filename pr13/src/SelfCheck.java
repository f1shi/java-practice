import java.time.Duration;
import java.time.LocalDateTime;

/** Самопроверка правил интервалов: базовая формула (без буфера) и буфер варианта 3 (20 минут). */
public class SelfCheck {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 9, 0);
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        // базовая формула пересечения: сервис с нулевым буфером
        BookingRepository plainRepo = new BookingRepository();
        BookingService plain = new BookingService(plainRepo, Duration.ZERO);
        Booking base = plain.book("R1", t(10, 0), t(11, 0), NOW);
        check("типовое бронирование 10:00-11:00, длительность 60 мин", base.duration().toMinutes() == 60);
        check("без буфера: стык 11:00-12:00 — не конфликт", plain.isAvailable("R1", t(11, 0), t(12, 0)));
        check("без буфера: стык 09:00-10:00 — не конфликт", plain.isAvailable("R1", t(9, 0), t(10, 0)));
        check("частичное слева 09:30-10:01 — конфликт", !plain.isAvailable("R1", t(9, 30), t(10, 1)));
        check("частичное справа 10:59-11:30 — конфликт", !plain.isAvailable("R1", t(10, 59), t(11, 30)));
        check("внутри 10:15-10:45 — конфликт", !plain.isAvailable("R1", t(10, 15), t(10, 45)));
        check("охватывает 09:00-12:00 — конфликт", !plain.isAvailable("R1", t(9, 0), t(12, 0)));
        check("совпадает 10:00-11:00 — конфликт", !plain.isAvailable("R1", t(10, 0), t(11, 0)));
        check("другая комната — не конфликт", plain.isAvailable("R2", t(10, 0), t(11, 0)));
        int before = plainRepo.count();
        plain.isAvailable("R1", t(12, 0), t(13, 0));
        check("isAvailable не меняет репозиторий", plainRepo.count() == before);

        // вариант 3: сервис по умолчанию — буфер 20 минут
        BookingService s = new BookingService(new BookingRepository());
        Booking first = s.book("R1", t(10, 0), t(11, 0), NOW);
        check("буфер: 11:00 (стык) — конфликт", !s.isAvailable("R1", t(11, 0), t(12, 0)));
        check("буфер: 11:19 — конфликт", !s.isAvailable("R1", t(11, 19), t(12, 0)));
        check("буфер: 11:20 — допустимо (граница)", s.isAvailable("R1", t(11, 20), t(12, 0)));
        check("буфер: окончание 09:41 — конфликт", !s.isAvailable("R1", t(9, 0), t(9, 41)));
        check("буфер: окончание 09:40 — допустимо (граница)", s.isAvailable("R1", t(9, 0), t(9, 40)));
        check("буфер не действует между разными комнатами", s.isAvailable("R2", t(11, 0), t(12, 0)));
        try {
            s.book("R1", t(11, 10), t(12, 0), NOW);
            check("book внутри буфера -> исключение", false);
        } catch (BookingConflictException e) {
            check("конфликт внутри буфера возвращает id " + e.getConflictingBookingId(),
                    e.getConflictingBookingId().equals(first.getId()));
        }
        expect("отрицательный буфер", () -> new BookingService(new BookingRepository(), Duration.ofMinutes(-1)));

        // длительность: 15 мин .. 8 ч
        expect("14 минут", () -> s.book("R3", t(12, 0), t(12, 14), NOW));
        check("ровно 15 минут", run(() -> s.book("R3", t(12, 0), t(12, 15), NOW)));
        check("ровно 8 часов", run(() -> s.book("R4", t(9, 0), t(17, 0), NOW)));
        expect("8 часов 1 минута", () -> s.book("R5", t(9, 0), t(17, 1), NOW));
        expect("окончание = начало", () -> s.book("R6", t(12, 0), t(12, 0), NOW));
        expect("окончание раньше начала", () -> s.book("R6", t(12, 0), t(11, 0), NOW));
        expect("начало в прошлом (08:59)", () -> s.book("R6", t(8, 59), t(9, 30), NOW));
        check("начало ровно сейчас допустимо", run(() -> s.book("R7", NOW, NOW.plusMinutes(30), NOW)));
        expect("null вместо времени", () -> s.book("R6", null, t(11, 0), NOW));
        expect("неверный формат времени", () -> BookingApp.parse("20.10.2026 10:00"));

        // переход через полночь
        LocalDateTime lateStart = LocalDateTime.of(2026, 10, 20, 23, 0);
        LocalDateTime nextDay = LocalDateTime.of(2026, 10, 21, 1, 0);
        check("23:00-01:00 следующего дня допустимо (120 мин)", run(() -> s.book("R8", lateStart, nextDay, NOW)));
        check("00:30-02:00 следующего дня конфликтует с ночным",
                !s.isAvailable("R8", nextDay.minusMinutes(30), nextDay.plusHours(1)));
        check("буфер через полночь: 01:10 — конфликт, 01:20 — допустимо",
                !s.isAvailable("R8", nextDay.plusMinutes(10), nextDay.plusHours(1))
                        && s.isAvailable("R8", nextDay.plusMinutes(20), nextDay.plusHours(1)));
        check("тот же час другого дня — не конфликт (сравниваются даты)",
                s.isAvailable("R1", t(10, 0).plusDays(1), t(11, 0).plusDays(1)));

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
