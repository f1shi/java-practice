/** Внутренний канал: логин сотрудника из латинских букв, цифр, '.', '_' или '-', длиной 3..32. */
public class InternalChannel implements NotificationChannel {

    @Override
    public ChannelType type() {
        return ChannelType.INTERNAL;
    }

    @Override
    public boolean supports(NotificationRequest request) {
        return request.recipient().matches("[a-z0-9._-]{3,32}");
    }

    @Override
    public String rejectionReason(NotificationRequest request) {
        return "логин должен содержать 3..32 символа [a-z0-9._-]: '" + request.recipient() + "'";
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        return NotificationResult.sent(type(), request.recipient(), "сообщение добавлено во внутренний ящик");
    }
}
