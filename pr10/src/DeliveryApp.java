import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Расчёт доставки для каждой строки data/sample.csv: type;distanceKm;weightKg */
public class DeliveryApp {

    public static void main(String[] args) throws IOException {
        DeliveryService service = new DeliveryService(DeliveryPolicyFactory.withDefaultPolicies());
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                double cost = service.calculate(parse(line));
                System.out.printf(Locale.forLanguageTag("ru-RU"), "строка %d %-22s -> %,.2f руб.%n", i + 1, line, cost);
            } catch (IllegalArgumentException | DeliveryNotPossibleException e) {
                System.out.printf("строка %d %-22s -> ОТКАЗ: %s%n", i + 1, line, e.getMessage());
            }
        }
    }

    static DeliveryRequest parse(String line) {
        String[] p = line.split(";", -1);
        if (p.length != 3) {
            throw new IllegalArgumentException("ожидалось 3 поля, получено " + p.length);
        }
        return new DeliveryRequest(DeliveryType.parse(p[0]), parseNumber(p[1], "расстояние"), parseNumber(p[2], "вес"));
    }

    private static double parseNumber(String raw, String field) {
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " имеет неверный формат: '" + raw.trim() + "'");
        }
    }
}
