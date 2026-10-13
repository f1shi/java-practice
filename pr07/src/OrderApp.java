import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Консольное приложение: загружает каталог data/products.csv и выполняет команды из data/sample.csv.
 * Команды: ADD;productId;quantity | REMOVE;productId | CONFIRM
 */
public class OrderApp {

    public static void main(String[] args) throws IOException {
        Path productsFile = Path.of(args.length > 0 ? args[0] : "data/products.csv");
        Path commandsFile = Path.of(args.length > 1 ? args[1] : "data/sample.csv");

        ProductCatalog catalog = loadCatalog(productsFile);
        OrderService service = new OrderService();
        Order order = service.createOrder(new Employee("E-001", "Иванов Иван Иванович"));

        List<String> lines = Files.readAllLines(commandsFile);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                execute(line, order, catalog, service);
                System.out.println("строка " + (i + 1) + " OK:    " + line);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("строка " + (i + 1) + " ОШИБКА: " + line + " -> " + e.getMessage());
            }
        }
        printOrder(order);
    }

    static ProductCatalog loadCatalog(Path file) throws IOException {
        ProductCatalog catalog = new ProductCatalog();
        List<String> lines = Files.readAllLines(file);
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(";", -1);
            catalog.add(new Product(p[0], p[1], Double.parseDouble(p[2])));
        }
        return catalog;
    }

    static void execute(String line, Order order, ProductCatalog catalog, OrderService service) {
        String[] p = line.split(";", -1);
        switch (p[0].trim().toUpperCase(Locale.ROOT)) {
            case "ADD" -> {
                requireFields(p, 3);
                order.addProduct(catalog.get(p[1].trim()), parseQuantity(p[2]));
            }
            case "REMOVE" -> {
                requireFields(p, 2);
                order.removeProduct(p[1].trim());
            }
            case "CONFIRM" -> service.confirm(order);
            default -> throw new IllegalArgumentException("неизвестная команда '" + p[0] + "'");
        }
    }

    private static void requireFields(String[] p, int count) {
        if (p.length != count) {
            throw new IllegalArgumentException("ожидалось полей: " + count + ", получено " + p.length);
        }
    }

    static int parseQuantity(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("количество не указано");
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("количество должно быть целым числом: '" + raw.trim() + "'");
        }
    }

    static void printOrder(Order order) {
        System.out.println();
        System.out.println("Заказ сотрудника " + order.getEmployee() + ", статус " + order.getStatus());
        for (OrderItem item : order.getItems()) {
            System.out.printf(Locale.forLanguageTag("ru-RU"), "  %-35s %3d шт. %,12.2f руб.%n",
                    item.getProduct().toString(), item.getQuantity(), item.getLineTotal());
        }
        System.out.printf(Locale.forLanguageTag("ru-RU"), "Итого: %,.2f руб.%n", order.total());
    }
}
