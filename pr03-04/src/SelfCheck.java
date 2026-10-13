import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Самопроверка: открытый набор data/sample.csv + собственные граничные проверки.
 * Запуск из каталога pr03-04: java -cp out SelfCheck
 */
public class SelfCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws IOException {
        checkSampleFile(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));

        // типовое значение: 10 * 1000 = 10000, скидка 5 % -> 9500 (< 20 000, обработка 500) -> 10000, НДС 20 % -> 12000
        checkTotal("типовое значение", 10, 1000.0, 5.0, 12000.00);
        // вариант 3: порог бесплатной обработки 20 000 (включается), сравнивается сумма после скидки
        checkTotal("ровно 20 000 -> обработка бесплатна", 1, 20_000.0, 0.0, 24000.00);
        checkTotal("19 999.99 -> обработка 500", 1, 19_999.99, 0.0, 24599.99);
        checkTotal("25 000 со скидкой 20 % = 20 000 -> бесплатно", 1, 25_000.0, 20.0, 24000.00);
        checkTotal("21 000 со скидкой 5 % = 19 950 -> платно", 1, 21_000.0, 5.0, 24540.00);
        check("processingFee(20000) = 0", OrderCalculator.processingFee(20_000.0) == 0.0);
        check("processingFee(19999.99) = 500", OrderCalculator.processingFee(19_999.99) == 500.0);
        // нижние границы (включаются)
        checkValid("количество 1 допустимо", 1, 100.0, 0.0, true);
        checkValid("количество 0 недопустимо", 0, 100.0, 0.0, false);
        checkValid("цена 0.01 допустима", 1, 0.01, 0.0, true);
        checkValid("цена 0 недопустима", 1, 0.0, 0.0, false);
        checkValid("скидка 0 допустима", 1, 100.0, 0.0, true);
        checkValid("скидка -0.01 недопустима", 1, 100.0, -0.01, false);
        // верхние границы (включаются)
        checkValid("количество 10000 допустимо", 10_000, 1.0, 0.0, true);
        checkValid("количество 10001 недопустимо", 10_001, 1.0, 0.0, false);
        checkValid("цена 5 000 000 допустима", 1, 5_000_000.0, 0.0, true);
        checkValid("цена 5 000 000.01 недопустима", 1, 5_000_000.01, 0.0, false);
        checkValid("скидка 30 допустима", 1, 100.0, 30.0, true);
        checkValid("скидка 30.01 недопустима", 1, 100.0, 30.01, false);
        // максимальная сумма не переполняет int, потому что считается в double
        checkTotal("максимальный заказ", 10_000, 5_000_000.0, 0.0, 60_000_000_000.00);
        // скидка — процент, а не абсолютная сумма: 1000 - 30 % = 700, + обработка 500 = 1200, + НДС 20 % = 1440
        checkTotal("скидка 30 % от 1000", 1, 1000.0, 30.0, 1440.00);
        // копейки: 3 * 0.1 в double = 0.30000000000000004, после округления 0.30
        check("округление до копеек", OrderCalculator.calculateBase(3, 0.1) == 0.30);
        // пустое значение и неверный формат -> контролируемая ошибка
        checkParseFails("пустое количество", "");
        checkParseFails("количество 'abc'", "abc");
        checkParseFails("дробное количество", "2.5");
        check("цена с запятой 99,90", OrderCalculator.parseNumber("99,90", "цена") == 99.90);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Формат строки: quantity;price;discount;expected (число или ERROR). */
    private static void checkSampleFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] p = line.split(";", -1);
            String name = "sample.csv строка " + (i + 1) + " [" + line + "]";
            try {
                int q = OrderCalculator.parseQuantity(p[0]);
                double price = OrderCalculator.parseNumber(p[1], "цена");
                double discount = OrderCalculator.parseNumber(p[2], "скидка");
                if (!OrderCalculator.isValid(q, price, discount)) {
                    check(name, p[3].equals("ERROR"));
                } else {
                    check(name, !p[3].equals("ERROR")
                            && OrderCalculator.calculateTotal(q, price, discount) == Double.parseDouble(p[3]));
                }
            } catch (IllegalArgumentException e) {
                check(name, p[3].equals("ERROR"));
            }
        }
    }

    private static void checkTotal(String name, int q, double price, double discount, double expected) {
        double actual = OrderCalculator.calculateTotal(q, price, discount);
        check(name + " (ожидалось " + expected + ", получено " + actual + ")", actual == expected);
    }

    private static void checkValid(String name, int q, double price, double discount, boolean expected) {
        check(name, OrderCalculator.isValid(q, price, discount) == expected);
    }

    private static void checkParseFails(String name, String text) {
        try {
            OrderCalculator.parseQuantity(text);
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
