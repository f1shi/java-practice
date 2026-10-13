/** Сервис уведомлений: общая проверка запроса и делегирование каналу. Деталей каналов не знает. */
public class NotificationService {

    static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChannelFactory factory;

    /** Зависимость передаётся через конструктор — в тестах можно подставить фабрику с фиктивным каналом. */
    public NotificationService(ChannelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("фабрика каналов обязательна");
        }
        this.factory = factory;
    }

    public NotificationResult notify(NotificationRequest request) {
        String commonError = commonValidationError(request);
        if (commonError != null) {
            ChannelType type = request == null ? null : request.channel();
            String recipient = request == null ? null : request.recipient();
            return NotificationResult.failed(type, recipient, commonError);
        }
        NotificationChannel channel = factory.get(request.channel());
        if (!channel.supports(request)) {
            return NotificationResult.failed(request.channel(), request.recipient(), channel.rejectionReason(request));
        }
        return channel.send(request);
    }

    /** A1: проверки, общие для всех каналов. */
    private static String commonValidationError(NotificationRequest request) {
        if (request == null) {
            return "запрос не передан";
        }
        if (request.channel() == null) {
            return "канал не указан";
        }
        if (request.recipient() == null || request.recipient().isBlank()) {
            return "адресат не указан";
        }
        if (request.message() == null || request.message().isBlank()) {
            return "текст сообщения пуст";
        }
        if (request.message().length() > MAX_MESSAGE_LENGTH) {
            return "сообщение длиннее " + MAX_MESSAGE_LENGTH + " символов";
        }
        return null;
    }
}
