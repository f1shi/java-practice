import java.util.List;

/** Самопроверка стратегий доставки. */
public class SelfCheck {

    private static final DeliveryService SERVICE = new DeliveryService(DeliveryPolicyFactory.withDefaultPolicies());
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        cost("самовывоз бесплатно", DeliveryType.PICKUP, 0, 12, 0.0);
        cost("стандарт 10 км 3 кг = 300 + 200", DeliveryType.STANDARD, 10, 3, 500.0);
        cost("стандарт 10 км 8 кг = 500 + 3*15", DeliveryType.STANDARD, 10, 8, 545.0);
        cost("стандарт ровно 5 кг без доплаты", DeliveryType.STANDARD, 0, 5, 300.0);
        cost("стандарт граница 100 км 30 кг", DeliveryType.STANDARD, 100, 30, 300 + 2000 + 25 * 15);
        impossible("стандарт 100.1 км", DeliveryType.STANDARD, 100.1, 1);
        impossible("стандарт 30.5 кг", DeliveryType.STANDARD, 1, 30.5);
        cost("экспресс 5 км -> минимум 800", DeliveryType.EXPRESS, 5, 2, 800.0);
        cost("экспресс 30 км = 500 + 1200", DeliveryType.EXPRESS, 30, 2, 1700.0);
        cost("экспресс граница 50 км 10 кг", DeliveryType.EXPRESS, 50, 10, 2500.0);
        impossible("экспресс 10.01 кг", DeliveryType.EXPRESS, 1, 10.01);
        // индивидуальный вариант 3: постамат, вес до 20 кг
        cost("постамат 5 км 3 кг = 250 + 30", DeliveryType.PARCEL_LOCKER, 5, 3, 280.0);
        cost("постамат граница 20 кг = 250 + 200", DeliveryType.PARCEL_LOCKER, 10, 20, 450.0);
        impossible("постамат 20.01 кг", DeliveryType.PARCEL_LOCKER, 10, 20.01);
        cost("постамат граница 30 км", DeliveryType.PARCEL_LOCKER, 30, 1, 260.0);
        impossible("постамат 30.1 км", DeliveryType.PARCEL_LOCKER, 30.1, 1);

        expectError("отрицательное расстояние", () -> new DeliveryRequest(DeliveryType.STANDARD, -1, 1));
        expectError("нулевой вес", () -> new DeliveryRequest(DeliveryType.STANDARD, 1, 0));
        expectError("тип null", () -> new DeliveryRequest(null, 1, 1));
        expectError("неизвестный тип DRONE", () -> DeliveryType.parse("DRONE"));
        expectError("пустой тип", () -> DeliveryType.parse(" "));
        expectError("расстояние 'abc'", () -> DeliveryApp.parse("STANDARD;abc;1"));
        check("регистр строки не важен", DeliveryType.parse(" express ") == DeliveryType.EXPRESS);
        expectError("две политики одного типа", () -> new DeliveryPolicyFactory(
                List.of(new PickupPolicy(), new PickupPolicy())));
        expectError("нет политики для типа", () -> new DeliveryPolicyFactory(List.of(new PickupPolicy()))
                .get(DeliveryType.EXPRESS));
        check("фабрика возвращает реализацию по типу",
                DeliveryPolicyFactory.withDefaultPolicies().get(DeliveryType.PARCEL_LOCKER) instanceof ParcelLockerPolicy);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void cost(String name, DeliveryType type, double km, double kg, double expected) {
        double actual = SERVICE.calculate(new DeliveryRequest(type, km, kg));
        check(name + " -> " + actual, actual == expected);
    }

    private static void impossible(String name, DeliveryType type, double km, double kg) {
        try {
            SERVICE.calculate(new DeliveryRequest(type, km, kg));
            check(name + " -> ожидался отказ", false);
        } catch (DeliveryNotPossibleException e) {
            check(name + " -> " + e.getMessage(), true);
        }
    }

    private static void expectError(String name, Runnable action) {
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
