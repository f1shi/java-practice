/** Запрос на отправку уведомления. */
public record NotificationRequest(ChannelType channel, String recipient, String message) {
}
