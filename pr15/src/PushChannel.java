/**
 * Канал, добавленный на шаге 5 без изменения существующих: push-уведомление на устройство.
 * Адресат — токен устройства из латинских букв и цифр длиной не менее 10 символов.
 */
public class PushChannel implements NotificationChannel {

    /** Минимальная длина токена устройства. */
    static final int MIN_TOKEN_LENGTH = 10;

    @Override
    public ChannelType type() {
        return ChannelType.PUSH;
    }

    @Override
    public boolean supports(NotificationRequest request) {
        String token = request.recipient();
        return token.length() >= MIN_TOKEN_LENGTH && token.matches("[A-Za-z0-9]+");
    }

    @Override
    public String rejectionReason(NotificationRequest request) {
        return "токен устройства должен содержать не менее " + MIN_TOKEN_LENGTH
                + " латинских букв/цифр: '" + request.recipient() + "'";
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        return NotificationResult.sent(type(), request.recipient(), "push отправлен на устройство");
    }
}
