import java.util.ArrayList;
import java.util.List;

/** Самопроверка каналов и сервиса, включая тест сервиса с фиктивным каналом через интерфейс. */
public class SelfCheck {

    private static final NotificationService SERVICE = new NotificationService(ChannelFactory.withDefaultChannels());
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        sent("email корректный", ChannelType.EMAIL, "ivanov@corp.ru", "Привет");
        rejected("email без @", ChannelType.EMAIL, "ivanov.corp.ru", "Привет");
        rejected("email без точки в домене", ChannelType.EMAIL, "ivanov@corp", "Привет");
        rejected("email с двумя @", ChannelType.EMAIL, "a@b@corp.ru", "Привет");
        sent("sms +7 и 10 цифр", ChannelType.SMS, "+79161234567", "Код 1234");
        rejected("sms 8XXXXXXXXXX", ChannelType.SMS, "89161234567", "Код 1234");
        sent("sms ровно 160 символов", ChannelType.SMS, "+79161234567", "x".repeat(160));
        rejected("sms 161 символ", ChannelType.SMS, "+79161234567", "x".repeat(161));
        sent("internal логин", ChannelType.INTERNAL, "a.petrova", "Совещание");
        rejected("internal кириллица", ChannelType.INTERNAL, "Петрова", "Совещание");
        rejected("internal 2 символа", ChannelType.INTERNAL, "ab", "Совещание");
        // вариант 1: PUSH, токен >= 10
        sent("push токен 10 символов", ChannelType.PUSH, "abcde12345", "Задача");
        rejected("push токен 9 символов", ChannelType.PUSH, "abcde1234", "Задача");
        rejected("push токен со спецсимволом", ChannelType.PUSH, "abcde12345!", "Задача");

        rejected("пустой адресат", ChannelType.EMAIL, " ", "Привет");
        rejected("пустое сообщение", ChannelType.EMAIL, "ivanov@corp.ru", "");
        rejected("сообщение 1001 символ", ChannelType.EMAIL, "ivanov@corp.ru", "x".repeat(1001));
        rejected("канал null", null, "ivanov@corp.ru", "Привет");
        check("запрос null -> отказ", !SERVICE.notify(null).success());
        expect("неизвестный канал FAX", () -> ChannelType.parse("FAX"));
        expect("канал зарегистрирован дважды",
                () -> new ChannelFactory(List.of(new SmsChannel(), new SmsChannel())));
        expect("канал не подключён", () -> new ChannelFactory(List.of(new SmsChannel())).get(ChannelType.EMAIL));
        expect("сервис без фабрики", () -> new NotificationService(null));

        // тестируемость через интерфейс: фиктивный канал вместо настоящего Email
        RecordingChannel fake = new RecordingChannel();
        NotificationService testService = new NotificationService(new ChannelFactory(List.of(fake)));
        NotificationResult result = testService.notify(new NotificationRequest(ChannelType.EMAIL, "any@x.ru", "Тест"));
        check("сервис вызывает send выбранного канала ровно один раз", result.success() && fake.sent.size() == 1);
        testService.notify(new NotificationRequest(ChannelType.EMAIL, "any@x.ru", " "));
        check("при ошибке общей проверки send не вызывается", fake.sent.size() == 1);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Фиктивная реализация канала для проверки NotificationService без реальной отправки. */
    private static final class RecordingChannel implements NotificationChannel {
        private final List<NotificationRequest> sent = new ArrayList<>();

        @Override
        public ChannelType type() {
            return ChannelType.EMAIL;
        }

        @Override
        public boolean supports(NotificationRequest request) {
            return true;
        }

        @Override
        public String rejectionReason(NotificationRequest request) {
            return "";
        }

        @Override
        public NotificationResult send(NotificationRequest request) {
            sent.add(request);
            return NotificationResult.sent(type(), request.recipient(), "записано фиктивным каналом");
        }
    }

    private static void sent(String name, ChannelType type, String recipient, String message) {
        NotificationResult r = SERVICE.notify(new NotificationRequest(type, recipient, message));
        check(name + " -> " + r, r.success());
    }

    private static void rejected(String name, ChannelType type, String recipient, String message) {
        NotificationResult r = SERVICE.notify(new NotificationRequest(type, recipient, message));
        check(name + " -> " + r, !r.success());
    }

    private static void expect(String name, Runnable action) {
        try {
            action.run();
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
