import java.util.List;
import java.util.Map;

/** Самопроверка склада. */
public class SelfCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        Warehouse w = new Warehouse();
        w.register(new StockItem("A-1", "Ноутбук", Category.LAPTOP));
        w.register(new StockItem("A-2", "Ноутбук 2", Category.LAPTOP));
        w.register(new StockItem("M-1", "Монитор", Category.MONITOR));
        w.receive("A-1", 25);
        w.receive("A-2", 5);
        w.receive("M-1", 12);

        w.issue("A-1", 5);
        check("типовая выдача: 25 - 5 = 20", w.find("A-1").getQuantity() == 20);

        // вариант 3: не более 20 единиц за одну выдачу
        expect("выдача 21 ед. за раз", () -> w.issue("A-1", 21), IllegalArgumentException.class);
        check("после отказа остаток не изменился (20)", w.find("A-1").getQuantity() == 20);
        w.issue("A-1", 20);
        check("выдача ровно 20 ед. (граница включается) -> остаток 0", w.find("A-1").getQuantity() == 0);
        expect("выдача при нулевом остатке", () -> w.issue("A-1", 1), IllegalStateException.class);
        expect("выдача больше остатка (5 при остатке 5 + 1)", () -> w.issue("A-2", 6), IllegalStateException.class);
        w.receive("A-1", 15);

        // некорректные количества
        expect("приход 0", () -> w.receive("M-1", 0), IllegalArgumentException.class);
        expect("приход отрицательный", () -> w.receive("M-1", -3), IllegalArgumentException.class);
        expect("выдача 0", () -> w.issue("M-1", 0), IllegalArgumentException.class);
        expect("неизвестный код", () -> w.issue("X-404", 1), IllegalArgumentException.class);
        expect("код null", () -> w.find(null), IllegalArgumentException.class);
        expect("пустой код при создании", () -> new StockItem(" ", "x", Category.LAPTOP), IllegalArgumentException.class);
        expect("категория null", () -> new StockItem("Z", "x", null), IllegalArgumentException.class);
        expect("неизвестная категория", () -> WarehouseApp.parseCategory("KITCHEN"), IllegalArgumentException.class);
        expect("количество 'abc'", () -> WarehouseApp.parseQuantity("abc"), IllegalArgumentException.class);

        // уникальность кода
        expect("повторный инвентарный код", () -> w.register(new StockItem("A-1", "Другой", Category.MONITOR)),
                IllegalArgumentException.class);
        check("после отказа исходная карточка не заменена", w.find("A-1").getCategory() == Category.LAPTOP);

        // операции фиксируются, только успешные
        check("зафиксировано 6 успешных операций", w.getMovements().size() == 6);
        check("последняя операция — RECEIPT A-1 15",
                w.getMovements().get(5).equals(new StockMovement("A-1", MovementType.RECEIPT, 15)));
        expect("журнал операций нельзя изменить", () -> w.getMovements().clear(), UnsupportedOperationException.class);

        // поиск возвращает копию
        List<StockItem> laptops = w.findByCategory(Category.LAPTOP);
        laptops.clear();
        check("очистка результата поиска не меняет склад", w.findByCategory(Category.LAPTOP).size() == 2);
        check("поиск по пустой категории -> пустой список", w.findByCategory(Category.FURNITURE).isEmpty());

        Map<Category, Integer> totals = w.totalsByCategory();
        check("сводка: LAPTOP = 20, MONITOR = 12", totals.get(Category.LAPTOP) == 20 && totals.get(Category.MONITOR) == 12);
        totals.put(Category.LAPTOP, 999);
        check("изменение сводки не влияет на склад", w.totalsByCategory().get(Category.LAPTOP) == 20);
        check("Set категорий без повторов: {LAPTOP, MONITOR}", w.usedCategories().size() == 2);
        check("equals по коду", new StockItem("A-1", "x", Category.NETWORK).equals(w.find("A-1")));

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
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
