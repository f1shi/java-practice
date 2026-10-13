import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Самопроверка: data/sample.csv (amount;urgent;risk;pd;expected) + таблица решений + границы. */
public class SelfCheck {

    private static final ApprovalRouter ROUTER = new ApprovalRouter();
    private static int passed;
    private static int failed;

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int cut = line.lastIndexOf(';');
            checkCsv("sample.csv строка " + (i + 1), line.substring(0, cut), line.substring(cut + 1));
        }

        long L = ApprovalRouter.FINANCE_LIMIT;
        // таблица решений: 10 сочетаний
        checkRoute("1 ПДн + HIGH + крупная + срочная", L + 1, true, RiskLevel.HIGH, true, ApprovalRoute.SECURITY_REVIEW);
        checkRoute("2 ПДн + малая сумма + LOW", 1_000, false, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW);
        checkRoute("3 ПДн + срочная (фрагмент из задания)", 1_000, true, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW);
        checkRoute("4 HIGH + крупная", L, false, RiskLevel.HIGH, false, ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("5 HIGH + срочная + малая", 1_000, true, RiskLevel.HIGH, false, ApprovalRoute.DIRECTOR_REVIEW);
        checkRoute("6 MEDIUM + крупная + срочная", L, true, RiskLevel.MEDIUM, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("7 LOW + крупная", L + 1, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("8 LOW + срочная + малая", L - 1, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("9 MEDIUM + не срочная + малая", L - 1, false, RiskLevel.MEDIUM, false, ApprovalRoute.STANDARD);
        checkRoute("10 сумма 0", 0, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        // точные границы базового правила (500 000) — при варианте 1 это уже не порог
        checkRoute("499 999 срочная", 499_999, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("500 000 срочная (вариант 1: ниже порога 550 000)", 500_000, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("500 001 не срочная", 500_001, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD);
        // границы порога варианта
        checkRoute("549 999 срочная -> FAST_TRACK", 549_999, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK);
        checkRoute("550 000 срочная -> FINANCE (>= включает)", 550_000, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        checkRoute("550 001 -> FINANCE", 550_001, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW);
        // ошибки
        checkCsv("отрицательная сумма", "-1;false;LOW;false", "ERROR");
        checkCsv("пустая строка", "", "ERROR");
        checkCsv("пустой риск", "100;false;;false", "ERROR");
        checkCsv("неизвестный риск", "100;false;CRITICAL;false", "ERROR");
        checkCsv("сумма не число", "10k;false;LOW;false", "ERROR");
        checkCsv("флаг не boolean", "100;maybe;LOW;false", "ERROR");
        checkCsv("лишнее поле", "100;false;LOW;false;x", "ERROR");

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void checkRoute(String name, long amount, boolean urgent, RiskLevel risk, boolean pd, ApprovalRoute expected) {
        ApprovalRoute actual = ROUTER.route(amount, urgent, risk, pd);
        check(name + " -> " + actual, actual == expected);
    }

    private static void checkCsv(String name, String line, String expected) {
        try {
            ApprovalRoute actual = ROUTER.routeFromCsv(line);
            check(name + " [" + line + "] -> " + actual, actual.name().equals(expected));
        } catch (IllegalArgumentException e) {
            check(name + " [" + line + "] -> ошибка: " + e.getMessage(), expected.equals("ERROR"));
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
