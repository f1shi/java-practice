/** Результат отправки: успех с описанием доставки или отказ с причиной. */
public record NotificationResult(boolean success, ChannelType channel, String recipient, String details) {

    public static NotificationResult sent(ChannelType channel, String recipient, String details) {
        return new NotificationResult(true, channel, recipient, details);
    }

    public static NotificationResult failed(ChannelType channel, String recipient, String reason) {
        return new NotificationResult(false, channel, recipient, reason);
    }

    @Override
    public String toString() {
        return (success ? "ОТПРАВЛЕНО " : "ОТКАЗ ") + channel + " -> " + recipient + ": " + details;
    }
}
