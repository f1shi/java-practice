import java.util.Locale;
import java.util.Scanner;

/**
 * Практическая работа №3-4. Расчёт базовой стоимости, скидки, НДС и итоговой суммы заказа.
 */
public class OrderCalculator {

    /** Ставка НДС в процентах. Индивидуальный вариант 1: ставка НДС = 10 %. Меняется только здесь. */
    static final double VAT_RATE = 10.0;

    static final int MIN_QUANTITY = 1;
    static final int MAX_QUANTITY = 10_000;
    static final double MAX_PRICE = 5_000_000.0;
    static final double MIN_DISCOUNT = 0.0;
    static final double MAX_DISCOUNT = 30.0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Количество единиц: ");
        String quantityText = scanner.nextLine();
        System.out.print("Цена одной единицы, руб.: ");
        String priceText = scanner.nextLine();
        System.out.print("Скидка, %: ");
        String discountText = scanner.nextLine();

        int quantity;
        double price;
        double discount;
        try {
            quantity = parseQuantity(quantityText);
            price = parseNumber(priceText, "цена");
            discount = parseNumber(discountText, "скидка");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return;
        }

        String error = validationError(quantity, price, discount);
        if (error != null) {
            System.out.println("Ошибка: " + error);
            return;
        }

        double base = calculateBase(quantity, price);
        double discounted = applyDiscount(base, discount);
        double vat = calculateVat(discounted, VAT_RATE);
        double total = round(discounted + vat);

        System.out.println("Базовая стоимость: " + formatRub(base));
        System.out.println("Скидка " + formatPercent(discount) + ": " + formatRub(base - discounted));
        System.out.println("Сумма после скидки: " + formatRub(discounted));
        System.out.println("НДС " + formatPercent(VAT_RATE) + ": " + formatRub(vat));
        System.out.println("Итого к оплате: " + formatRub(total));
    }

    /** Базовая стоимость = количество * цена, округлённая до копеек. */
    public static double calculateBase(int quantity, double price) {
        return round(quantity * price);
    }

    /** Сумма после скидки. discountPercent трактуется как процент, а не как абсолютная сумма. */
    public static double applyDiscount(double base, double discountPercent) {
        double discountAmount = round(base * discountPercent / 100.0);
        return round(base - discountAmount);
    }

    /** Сумма НДС, начисленная на сумму после скидки. */
    public static double calculateVat(double amountAfterDiscount, double vatRatePercent) {
        return round(amountAfterDiscount * vatRatePercent / 100.0);
    }

    /** Итоговая сумма: база -> скидка -> НДС. Используется и в SelfCheck, чтобы формула не дублировалась. */
    public static double calculateTotal(int quantity, double price, double discountPercent) {
        double discounted = applyDiscount(calculateBase(quantity, price), discountPercent);
        return round(discounted + calculateVat(discounted, VAT_RATE));
    }

    public static boolean isValid(int quantity, double price, double discountPercent) {
        return validationError(quantity, price, discountPercent) == null;
    }

    /** Возвращает текст первой найденной ошибки или null, если данные корректны. */
    public static String validationError(int quantity, double price, double discountPercent) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            return "количество должно быть от " + MIN_QUANTITY + " до " + MAX_QUANTITY + ", получено " + quantity;
        }
        if (Double.isNaN(price) || price <= 0 || price > MAX_PRICE) {
            return "цена должна быть больше 0 и не больше " + formatRub(MAX_PRICE) + ", получено " + price;
        }
        if (Double.isNaN(discountPercent) || discountPercent < MIN_DISCOUNT || discountPercent > MAX_DISCOUNT) {
            return "скидка должна быть от 0 до 30 %, получено " + discountPercent;
        }
        return null;
    }

    static int parseQuantity(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("количество не указано");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("количество должно быть целым числом: '" + text.trim() + "'");
        }
    }

    static double parseNumber(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не указана");
        }
        try {
            // Допускаем и точку, и запятую как десятичный разделитель.
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " имеет неверный формат: '" + text.trim() + "'");
        }
    }

    /** Округление до копеек по правилу half-up. */
    static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    static String formatRub(double value) {
        return String.format(Locale.forLanguageTag("ru-RU"), "%,.2f руб.", value);
    }

    private static String formatPercent(double value) {
        return String.format(Locale.forLanguageTag("ru-RU"), "%.2f %%", value);
    }
}
