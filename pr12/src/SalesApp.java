import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Загружает data/sample.csv (id;manager;region;amount) и печатает аналитику. */
public class SalesApp {

    public static void main(String[] args) throws IOException {
        List<Sale> sales = load(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        SalesAnalyticsService service = new SalesAnalyticsService();

        System.out.println("Загружено продаж: " + sales.size());
        System.out.println("Топ-3: " + service.topSales(sales, 3));
        System.out.println("Максимальная: " + service.maxSale(sales).map(Sale::toString).orElse("нет продаж"));
        System.out.println("Итоги по менеджерам: " + service.totalsByManager(sales));
        System.out.printf(Locale.ROOT, "Среднее по региону Москва: %.2f%n", service.averageByRegion(sales, "Москва"));
        System.out.println("Крупные сделки (>= " + SalesAnalyticsService.LARGE_DEAL_THRESHOLD + "): "
                + service.largeSales(sales).size());
    }

    static List<Sale> load(Path file) throws IOException {
        List<Sale> result = new ArrayList<>();
        List<String> lines = Files.readAllLines(file);
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(";", -1);
            try {
                if (p.length != 4) {
                    throw new IllegalArgumentException("ожидалось 4 поля, получено " + p.length);
                }
                result.add(new Sale(p[0], p[1], p[2], parseAmount(p[3])));
            } catch (IllegalArgumentException e) {
                System.out.println("строка " + (i + 1) + " пропущена: " + e.getMessage());
            }
        }
        return result;
    }

    private static double parseAmount(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("сумма имеет неверный формат: '" + raw.trim() + "'");
        }
    }
}
