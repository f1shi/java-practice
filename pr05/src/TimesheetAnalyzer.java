import java.util.Locale;
import java.util.Scanner;

/**
 * Практическая работа №5. Анализ строки недельного табеля "8;9;7;10;8".
 * Stream API намеренно не используется.
 */
public class TimesheetAnalyzer {

    static final int DAYS_IN_WEEK = 5;
    static final int MIN_HOURS = 0;
    static final int MAX_HOURS = 16;
    /** Индивидуальный вариант 1: норма часов в день = 10. Переработкой считается день, где часов больше нормы. */
    static final int DAILY_NORM = 10;
    static final int WEEKLY_NORM = 40;
    static final String SEPARATOR = ";";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите часы за 5 дней через ';': ");
        String line = scanner.hasNextLine() ? scanner.nextLine() : "";
        try {
            int[] hours = parseHours(line);
            System.out.println(report(hours));
        } catch (IllegalArgumentException e) {
            System.out.println("Табель некорректен: " + e.getMessage());
        }
    }

    /**
     * Строгий разбор: ровно 5 целых значений от 0 до 16.
     * Ошибка любого значения делает табель некорректным; сообщение содержит номер позиции (с 1).
     */
    public static int[] parseHours(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("строка табеля пустая");
        }
        // limit -1 сохраняет пустые элементы в конце: "8;8;8;8;" даст 5 элементов, последний пустой
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != DAYS_IN_WEEK) {
            throw new IllegalArgumentException("ожидалось " + DAYS_IN_WEEK + " значений, получено " + parts.length);
        }
        int[] hours = new int[DAYS_IN_WEEK];
        for (int i = 0; i < parts.length; i++) {
            hours[i] = parseDay(parts[i], i + 1);
        }
        return hours;
    }

    private static int parseDay(String raw, int position) {
        String value = raw.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("позиция " + position + ": значение отсутствует");
        }
        int hours;
        try {
            hours = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("позиция " + position + ": '" + value + "' не является целым числом");
        }
        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new IllegalArgumentException("позиция " + position + ": " + hours
                    + " вне диапазона " + MIN_HOURS + ".." + MAX_HOURS);
        }
        return hours;
    }

    public static int total(int[] hours) {
        int sum = 0;
        for (int i = 0; i < hours.length; i++) {
            sum += hours[i];
        }
        return sum;
    }

    public static double average(int[] hours) {
        if (hours.length == 0) {
            return 0.0;
        }
        return (double) total(hours) / hours.length;
    }

    public static int overtimeDays(int[] hours) {
        int count = 0;
        int i = 0;
        while (i < hours.length) {
            if (hours[i] > DAILY_NORM) {
                count++;
            }
            i++;
        }
        return count;
    }

    public static int weeklyOvertime(int[] hours) {
        return Math.max(0, total(hours) - WEEKLY_NORM);
    }

    /** A2: итоговый отчёт из четырёх показателей. */
    public static String report(int[] hours) {
        return String.format(Locale.forLanguageTag("ru-RU"),
                "Сумма часов: %d%nСреднее в день: %.2f%nДней с переработкой (> %d ч): %d%nНедельная переработка: %d ч",
                total(hours), average(hours), DAILY_NORM, overtimeDays(hours), weeklyOvertime(hours));
    }
}
