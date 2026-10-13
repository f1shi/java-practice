/** Сервис уведомлений: общая проверка запроса и делегирование каналу. Деталей каналов не знает. */
public class NotificationService {

    static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChannelFactory factory;
    private final FallbackPolicy fallback;

    public NotificationService(ChannelFactory factory) {
        this(factory, FallbackPolicy.defaultPolicy());
    }

    /** Зависимости передаются через конструктор — в тестах можно подставить фабрику с фиктивным каналом. */
    public NotificationService(ChannelFactory factory, FallbackPolicy fallback) {
        if (factory == null) {
            throw new IllegalArgumentException("фабрика каналов обязательна");
        }
        if (fallback == null) {
            throw new IllegalArgumentException("политика резервного канала обязательна");
        }
        this.factory = factory;
        this.fallback = fallback;
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
            return tryFallback(request, channel.rejectionReason(request));
        }
        return channel.send(request);
    }

    /** Вариант 3: резервная отправка, если основной канал отказал. */
    private NotificationResult tryFallback(NotificationRequest request, String primaryReason) {
        if (!request.hasFallback() || request.channel() == fallback.channel()) {
            return NotificationResult.failed(request.channel(), request.recipient(), primaryReason);
        }
        NotificationRequest backup = new NotificationRequest(fallback.channel(), request.fallbackRecipient().trim(),
                request.message());
        NotificationChannel backupChannel = factory.get(fallback.channel());
        if (!backupChannel.supports(backup)) {
            return NotificationResult.failed(request.channel(), request.recipient(), primaryReason
                    + "; резервный канал: " + backupChannel.rejectionReason(backup));
        }
        if (!fallback.tryAcquire()) {
            return NotificationResult.failed(request.channel(), request.recipient(), primaryReason
                    + "; лимит резервных отправок (" + fallback.limit() + ") исчерпан");
        }
        NotificationResult sent = backupChannel.send(backup);
        return NotificationResult.sent(sent.channel(), sent.recipient(),
                "через резервный канал (" + request.channel() + " отказал: " + primaryReason + ")");
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
