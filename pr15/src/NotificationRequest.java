/**
 * Запрос на отправку уведомления. fallbackRecipient — логин сотрудника для резервного канала (может быть null).
 */
public record NotificationRequest(ChannelType channel, String recipient, String message, String fallbackRecipient) {

    public NotificationRequest(ChannelType channel, String recipient, String message) {
        this(channel, recipient, message, null);
    }

    public boolean hasFallback() {
        return fallbackRecipient != null && !fallbackRecipient.isBlank();
    }
}
