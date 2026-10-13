import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Самопроверка: data/sample.csv + собственные проверки.
 * Формат sample.csv: "табель|ожидание", где ожидание = "total,overtimeDays,weeklyOvertime" или "ERROR:позиция".
 */
public class SelfCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split("\\|", -1);
            checkLine("sample.csv строка " + (i + 1) + " [" + p[0] + "]", p[0], p[1]);
        }

        checkLine("типовой табель", "8;8;8;8;8", "40,0,0");
        checkLine("нижняя граница 0 входит", "0;0;0;0;0", "0,0,0");
        checkLine("верхняя граница 16 входит", "16;16;16;16;16", "80,5,40");
        checkLine("значение 17 вне диапазона", "8;8;17;8;8", "ERROR:позиция 3");
        checkLine("значение -1 вне диапазона", "-1;8;8;8;8", "ERROR:позиция 1");
        checkLine("ровно норма 10 — не переработка", "10;10;10;10;0", "40,0,0");
        checkLine("11 > нормы 10 — переработка", "11;8;8;8;8", "43,1,3");
        checkLine("пустая строка", "", "ERROR:пустая");
        checkLine("4 значения", "8;8;8;8", "ERROR:получено 4");
        checkLine("6 значений", "8;8;8;8;8;8", "ERROR:получено 6");
        checkLine("пропущенное значение (;;)", "8;;8;8;8", "ERROR:позиция 2");
        checkLine("лишний разделитель в конце", "8;8;8;8;", "ERROR:позиция 5");
        checkLine("дробное значение", "8;7.5;8;8;8", "ERROR:позиция 2");
        checkLine("буквы", "8;8;8;8;abc", "ERROR:позиция 5");
        checkLine("пробелы допускаются", " 8 ; 9 ;8;8; 7", "40,0,0");
        int[] h = {7, 8, 9, 8, 8};
        check("среднее 40/5 = 8.0", TimesheetAnalyzer.average(h) == 8.0);
        check("среднее 41/5 = 8.2", TimesheetAnalyzer.average(new int[]{9, 8, 8, 8, 8}) == 8.2);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void checkLine(String name, String line, String expected) {
        try {
            int[] hours = TimesheetAnalyzer.parseHours(line);
            String actual = TimesheetAnalyzer.total(hours) + "," + TimesheetAnalyzer.overtimeDays(hours)
                    + "," + TimesheetAnalyzer.weeklyOvertime(hours);
            check(name + " -> " + Arrays.toString(hours) + " = " + actual, actual.equals(expected));
        } catch (IllegalArgumentException e) {
            boolean ok = expected.startsWith("ERROR:") && e.getMessage().contains(expected.substring(6));
            check(name + " -> ошибка: " + e.getMessage(), ok);
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
