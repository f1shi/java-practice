import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Сценарий из data/sample.csv: REGISTER;code;name;category | RECEIVE;code;qty | ISSUE;code;qty */
public class WarehouseApp {

    public static void main(String[] args) throws IOException {
        Warehouse warehouse = new Warehouse();
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                execute(warehouse, line.split(";", -1));
                System.out.println("строка " + (i + 1) + " OK:    " + line);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("строка " + (i + 1) + " ОШИБКА: " + line + " -> " + e.getMessage());
            }
        }
        System.out.println();
        System.out.println("Ноутбуки: " + warehouse.findByCategory(Category.LAPTOP));
        System.out.println("Сводка по категориям: " + warehouse.totalsByCategory());
        System.out.println("Используемые категории: " + warehouse.usedCategories());
        System.out.println("Операций зафиксировано: " + warehouse.getMovements().size());
    }

    static void execute(Warehouse warehouse, String[] p) {
        switch (p[0].trim().toUpperCase(Locale.ROOT)) {
            case "REGISTER" -> {
                requireFields(p, 4);
                warehouse.register(new StockItem(p[1], p[2], parseCategory(p[3])));
            }
            case "RECEIVE" -> {
                requireFields(p, 3);
                warehouse.receive(p[1], parseQuantity(p[2]));
            }
            case "ISSUE" -> {
                requireFields(p, 3);
                warehouse.issue(p[1], parseQuantity(p[2]));
            }
            default -> throw new IllegalArgumentException("неизвестная команда '" + p[0] + "'");
        }
    }

    private static void requireFields(String[] p, int count) {
        if (p.length != count) {
            throw new IllegalArgumentException("ожидалось полей: " + count + ", получено " + p.length);
        }
    }

    static Category parseCategory(String raw) {
        try {
            return Category.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("неизвестная категория '" + raw.trim() + "'");
        }
    }

    static int parseQuantity(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("количество должно быть целым числом: '" + raw.trim() + "'");
        }
    }
}
