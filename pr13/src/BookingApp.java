import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Выполняет заявки на бронирование из data/sample.csv: room;start;end (ISO, например 2026-10-20T10:00). */
public class BookingApp {

    static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 9, 0);

    public static void main(String[] args) throws IOException {
        BookingService service = new BookingService(new BookingRepository());
        System.out.println("Текущее время (clockNow): " + NOW);
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                String[] p = line.split(";", -1);
                if (p.length != 3) {
                    throw new IllegalArgumentException("ожидалось 3 поля, получено " + p.length);
                }
                Booking b = service.book(p[0].trim(), parse(p[1]), parse(p[2]), NOW);
                System.out.println("строка " + (i + 1) + " СОЗДАНО " + b);
            } catch (BookingConflictException e) {
                System.out.println("строка " + (i + 1) + " КОНФЛИКТ с " + e.getConflictingBookingId() + ": " + line);
            } catch (IllegalArgumentException e) {
                System.out.println("строка " + (i + 1) + " ОШИБКА " + line + " -> " + e.getMessage());
            }
        }
    }

    static LocalDateTime parse(String raw) {
        try {
            return LocalDateTime.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("время '" + raw.trim() + "' не в формате ГГГГ-ММ-ДДTчч:мм");
        }
    }
}
