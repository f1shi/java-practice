import java.util.Locale;

/**
 * Практическая работа №6. Выбор маршрута согласования служебной заявки.
 * Проверки идут от наиболее строгого правила к общему, первое сработавшее правило определяет маршрут.
 */
public class ApprovalRouter {

    /** Порог финансового согласования, руб. */
    static final long FINANCE_LIMIT = 500_000L;
    /**
     * Индивидуальный вариант 3 — поведение срочной заявки (модификатор 20):
     * FAST_TRACK доступен только срочным заявкам на сумму не более 20 % порога финансового согласования
     * (100 000 руб.). Более крупные срочные заявки ниже порога идут по STANDARD.
     */
    static final int FAST_TRACK_PERCENT_OF_LIMIT = 20;
    static final long FAST_TRACK_MAX_AMOUNT = FINANCE_LIMIT * FAST_TRACK_PERCENT_OF_LIMIT / 100;

    public ApprovalRoute route(long amount, boolean urgent, RiskLevel risk, boolean containsPersonalData) {
        validate(amount, risk);
        if (containsPersonalData) {
            return ApprovalRoute.SECURITY_REVIEW;   // 1. персональные данные — независимо от суммы и риска
        }
        if (risk == RiskLevel.HIGH) {
            return ApprovalRoute.DIRECTOR_REVIEW;   // 2. высокий риск
        }
        if (requiresFinanceReview(amount)) {
            return ApprovalRoute.FINANCE_REVIEW;    // 3. крупная сумма, срочность не ускоряет
        }
        if (urgent && qualifiesForFastTrack(amount)) {
            return ApprovalRoute.FAST_TRACK;        // 4. срочная и сумма не больше 100 000 (вариант 3)
        }
        return ApprovalRoute.STANDARD;              // 5. все остальные случаи
    }

    static boolean requiresFinanceReview(long amount) {
        return amount >= FINANCE_LIMIT;
    }

    /** Вариант 3: ускоренный маршрут только для небольших срочных сумм (граница включается). */
    static boolean qualifiesForFastTrack(long amount) {
        return amount <= FAST_TRACK_MAX_AMOUNT;
    }

    private static void validate(long amount, RiskLevel risk) {
        if (amount < 0) {
            throw new IllegalArgumentException("сумма не может быть отрицательной: " + amount);
        }
        if (risk == null) {
            throw new IllegalArgumentException("уровень риска не указан");
        }
    }

    /** Разбор строки CSV "amount;urgent;risk;personalData" с контролируемыми ошибками. */
    public ApprovalRoute routeFromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("пустая строка заявки");
        }
        String[] p = line.split(";", -1);
        if (p.length != 4) {
            throw new IllegalArgumentException("ожидалось 4 поля, получено " + p.length);
        }
        long amount;
        try {
            amount = Long.parseLong(p[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("сумма имеет неверный формат: '" + p[0].trim() + "'");
        }
        return route(amount, parseBoolean(p[1], "urgent"), parseRisk(p[2]), parseBoolean(p[3], "personalData"));
    }

    private static boolean parseBoolean(String raw, String field) {
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if (v.equals("true") || v.equals("да")) {
            return true;
        }
        if (v.equals("false") || v.equals("нет")) {
            return false;
        }
        throw new IllegalArgumentException(field + ": ожидалось true/false, получено '" + raw.trim() + "'");
    }

    private static RiskLevel parseRisk(String raw) {
        String v = raw.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("уровень риска не указан");
        }
        try {
            return RiskLevel.valueOf(v);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("неизвестный уровень риска: '" + raw.trim() + "'");
        }
    }

    public static void main(String[] args) {
        ApprovalRouter router = new ApprovalRouter();
        java.util.Scanner in = new java.util.Scanner(System.in);
        System.out.print("Заявка (сумма;срочная;риск;перс.данные), например 80000;true;LOW;false: ");
        String line = in.hasNextLine() ? in.nextLine() : "";
        try {
            System.out.println("Маршрут: " + router.routeFromCsv(line));
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
