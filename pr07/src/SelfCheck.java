import java.util.List;

/** Самопроверка инвариантов заказа. */
public class SelfCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        Product laptop = new Product("P-100", "Ноутбук", 1000.00);
        Product mouse = new Product("P-400", "Мышь", 10.50);
        Employee employee = new Employee("E-1", "Петров П.П.");
        OrderService service = new OrderService();

        Order order = service.createOrder(employee);
        order.addProduct(laptop, 2);
        order.addProduct(mouse, 3);
        check("типовой заказ: итог 2*1000 + 3*10.5 = 2031.50", order.total() == 2031.50);

        order.addProduct(laptop, 3);
        check("повторный товар объединён в одну строку", order.getItems().size() == 2
                && order.getItems().get(0).getQuantity() == 5);
        check("итог пересчитан из состава: 5*1000 + 31.5 = 5031.50", order.total() == 5031.50);

        order.removeProduct("P-400");
        check("удаление по id товара", order.getItems().size() == 1 && order.total() == 5000.00);

        // границы количества (вариант 1: 1..10)
        expectError("количество 0", () -> service.createOrder(employee).addProduct(laptop, 0));
        expectOk("количество 1", () -> service.createOrder(employee).addProduct(laptop, 1));
        expectOk("количество 10 (верхняя граница варианта)", () -> service.createOrder(employee).addProduct(laptop, 10));
        expectError("количество 11", () -> service.createOrder(employee).addProduct(laptop, 11));
        expectError("объединение 6 + 5 = 11 > 10", () -> {
            Order o = service.createOrder(employee);
            o.addProduct(laptop, 6);
            o.addProduct(laptop, 5);
        });
        Order merged = service.createOrder(employee);
        merged.addProduct(laptop, 6);
        try {
            merged.addProduct(laptop, 5);
        } catch (IllegalArgumentException ignored) {
            // ожидаемо
        }
        check("неудачное объединение не изменило количество (6)", merged.getItems().get(0).getQuantity() == 6);

        // пустые и неверные значения
        expectError("товар null", () -> service.createOrder(employee).addProduct(null, 1));
        expectError("цена 0", () -> new Product("P-1", "x", 0));
        expectError("цена отрицательная", () -> new Product("P-1", "x", -1));
        expectError("пустой id товара", () -> new Product(" ", "x", 1));
        expectError("сотрудник null", () -> new Order(null));
        expectError("удаление отсутствующего товара", () -> service.createOrder(employee).removeProduct("P-777"));
        expectError("количество 'abc'", () -> OrderApp.parseQuantity("abc"));
        // уникальность id в каталоге
        ProductCatalog catalog = new ProductCatalog();
        catalog.add(laptop);
        expectError("повторный id в каталоге", () -> catalog.add(new Product("P-100", "Другой", 5)));

        // подтверждение и неизменяемость
        expectError("подтверждение пустого заказа", () -> service.confirm(service.createOrder(employee)));
        service.confirm(order);
        check("статус CONFIRMED", order.getStatus() == OrderStatus.CONFIRMED);
        expectError("add после confirm", () -> order.addProduct(mouse, 1));
        expectError("remove после confirm", () -> order.removeProduct("P-100"));
        expectError("повторный confirm", () -> service.confirm(order));
        expectError("внешнее изменение getItems()", () -> order.getItems().clear());
        List<OrderItem> view = order.getItems();
        check("состав не изменился после попыток", view.size() == 1 && order.total() == 5000.00);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void expectError(String name, Runnable action) {
        try {
            action.run();
            check(name + " -> ожидалась ошибка", false);
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
            check(name + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage(), true);
        }
    }

    private static void expectOk(String name, Runnable action) {
        try {
            action.run();
            check(name, true);
        } catch (RuntimeException e) {
            check(name + " -> неожиданная ошибка " + e.getMessage(), false);
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
