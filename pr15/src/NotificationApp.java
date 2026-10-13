import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Отправляет уведомления из data/sample.csv: channel;recipient;fallbackLogin;message (fallbackLogin может быть пустым) */
public class NotificationApp {

    public static void main(String[] args) throws IOException {
        NotificationService service = new NotificationService(ChannelFactory.withDefaultChannels());
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(";", 4);
            try {
                if (p.length != 4) {
                    throw new IllegalArgumentException("ожидалось 4 поля, получено " + p.length);
                }
                NotificationRequest request = new NotificationRequest(ChannelType.parse(p[0]), p[1].trim(), p[3],
                        p[2].isBlank() ? null : p[2].trim());
                System.out.println("строка " + (i + 1) + " " + service.notify(request));
            } catch (IllegalArgumentException e) {
                System.out.println("строка " + (i + 1) + " ОШИБКА: " + e.getMessage());
            }
        }
    }
}
