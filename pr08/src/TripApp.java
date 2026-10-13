import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Выполняет сценарий из data/sample.csv. Команды:
 * CREATE;id;employeeId;ФИО;start;end;назначение;цель | SUBMIT;id | APPROVE;id | REJECT;id;причина
 * | CANCEL;id | COMPLETE;id;сегодня
 */
public class TripApp {

    public static void main(String[] args) throws IOException {
        BusinessTripService service = new BusinessTripService();
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                execute(service, line.split(";", -1));
                System.out.println("строка " + (i + 1) + " OK:     " + line);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("строка " + (i + 1) + " ОТКАЗ:  " + line + " -> " + e.getMessage());
            }
        }
        System.out.println();
        for (TripStatus status : TripStatus.values()) {
            System.out.println(status + ": " + service.findByStatus(status));
        }
    }

    static void execute(BusinessTripService service, String[] p) {
        String command = p[0].trim().toUpperCase(Locale.ROOT);
        switch (command) {
            case "CREATE" -> {
                requireFields(p, 8);
                service.create(p[1], new Employee(p[2], p[3]), parseDate(p[4]), parseDate(p[5]), p[6], p[7]);
            }
            case "SUBMIT" -> service.get(field(p, 1)).submit();
            case "APPROVE" -> service.get(field(p, 1)).approve();
            case "REJECT" -> service.get(field(p, 1)).reject(p.length > 2 ? p[2] : null);
            case "CANCEL" -> service.get(field(p, 1)).cancel();
            case "COMPLETE" -> service.get(field(p, 1)).complete(parseDate(field(p, 2)));
            default -> throw new IllegalArgumentException("неизвестная команда '" + p[0] + "'");
        }
    }

    private static String field(String[] p, int index) {
        if (p.length <= index || p[index].isBlank()) {
            throw new IllegalArgumentException("не заполнено поле №" + (index + 1));
        }
        return p[index].trim();
    }

    private static void requireFields(String[] p, int count) {
        if (p.length != count) {
            throw new IllegalArgumentException("ожидалось полей: " + count + ", получено " + p.length);
        }
    }

    static LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("дата '" + raw.trim() + "' не в формате ГГГГ-ММ-ДД");
        }
    }
}
