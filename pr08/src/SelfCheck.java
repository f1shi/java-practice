import java.time.LocalDate;

/** Самопроверка таблицы переходов и инвариантов BusinessTrip. */
public class SelfCheck {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate END = LocalDate.of(2026, 10, 5);
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        // корректный путь DRAFT -> SUBMITTED -> APPROVED -> COMPLETED
        BusinessTrip t = filled();
        t.submit();
        t.approve();
        t.complete(END.plusDays(1));
        check("типовой путь до COMPLETED", t.getStatus() == TripStatus.COMPLETED);

        // все недопустимые переходы из каждого состояния
        expectState("approve из DRAFT", () -> filled().approve());
        expectState("reject из DRAFT", () -> filled().reject("Достаточно длинная причина"));
        expectState("complete из DRAFT", () -> filled().complete(END.plusDays(1)));
        expectState("submit из SUBMITTED", () -> submitted().submit());
        expectState("complete из SUBMITTED", () -> submitted().complete(END.plusDays(1)));
        expectState("approve из APPROVED", () -> approved().approve());
        expectState("submit из APPROVED", () -> approved().submit());
        expectState("cancel из COMPLETED", () -> completed().cancel());
        expectState("approve из REJECTED", () -> rejected().approve());
        expectState("submit из CANCELLED", () -> cancelled().submit());
        expectState("изменение данных после SUBMITTED", () -> submitted().updateDetails("Омск", "x", START, END));

        // разрешённые отмены
        check("cancel из DRAFT", run(() -> filled().cancel()));
        check("cancel из SUBMITTED", run(() -> submitted().cancel()));
        check("cancel из APPROVED", run(() -> approved().cancel()));

        // обязательные данные
        BusinessTrip empty = new BusinessTrip("T-9", new Employee("E", "Тест"), START, END);
        expectArg("submit без назначения", empty::submit);
        check("после ошибки статус остался DRAFT", empty.getStatus() == TripStatus.DRAFT);

        // причина отклонения: граница 10 символов (вариант 1)
        expectArg("причина 9 символов", () -> submitted().reject("123456789"));
        expectArg("причина из пробелов", () -> submitted().reject("            "));
        expectArg("причина null", () -> submitted().reject(null));
        BusinessTrip r = submitted();
        r.reject("1234567890");
        check("причина ровно 10 символов принята", r.getStatus() == TripStatus.REJECTED
                && r.getRejectionReason().equals("1234567890"));

        // даты
        expectArg("окончание раньше начала", () -> new BusinessTrip("T", new Employee("E", "Тест"), END, START));
        check("однодневная командировка start == end", run(() -> new BusinessTrip("T", new Employee("E", "Тест"), START, START)));
        expectState("complete в день окончания", () -> approved().complete(END));
        check("complete на следующий день после окончания", run(() -> approved().complete(END.plusDays(1))));
        expectArg("дата неверного формата", () -> TripApp.parseDate("05.10.2026"));

        // сервис
        BusinessTripService service = new BusinessTripService();
        service.create("T-1", new Employee("E", "Тест"), START, END, "Казань", "Конференция");
        expectArg("повтор id в сервисе", () -> service.create("T-1", new Employee("E", "Тест"), START, END, "Омск", "x"));
        check("findByStatus(DRAFT) = 1", service.findByStatus(TripStatus.DRAFT).size() == 1);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static BusinessTrip filled() {
        BusinessTrip trip = new BusinessTrip("T-1", new Employee("E-1", "Иванов"), START, END);
        trip.updateDetails("Казань", "Конференция", START, END);
        return trip;
    }

    private static BusinessTrip submitted() {
        BusinessTrip trip = filled();
        trip.submit();
        return trip;
    }

    private static BusinessTrip approved() {
        BusinessTrip trip = submitted();
        trip.approve();
        return trip;
    }

    private static BusinessTrip completed() {
        BusinessTrip trip = approved();
        trip.complete(END.plusDays(1));
        return trip;
    }

    private static BusinessTrip rejected() {
        BusinessTrip trip = submitted();
        trip.reject("Нет бюджета на квартал");
        return trip;
    }

    private static BusinessTrip cancelled() {
        BusinessTrip trip = filled();
        trip.cancel();
        return trip;
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

    private static void expectState(String name, Runnable action) {
        expect(name, action, IllegalStateException.class);
    }

    private static void expectArg(String name, Runnable action) {
        expect(name, action, IllegalArgumentException.class);
    }

    private static void expect(String name, Runnable action, Class<? extends RuntimeException> type) {
        try {
            action.run();
            check(name + " -> ожидалось " + type.getSimpleName(), false);
        } catch (RuntimeException e) {
            check(name + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage(), type.isInstance(e));
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
