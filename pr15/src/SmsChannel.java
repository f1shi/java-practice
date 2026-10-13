/** SMS: номер в формате +7XXXXXXXXXX, текст не длиннее 160 символов. */
public class SmsChannel implements NotificationChannel {

    static final int MAX_LENGTH = 160;

    @Override
    public ChannelType type() {
        return ChannelType.SMS;
    }

    @Override
    public boolean supports(NotificationRequest request) {
        return request.recipient().matches("\\+7\\d{10}") && request.message().length() <= MAX_LENGTH;
    }

    @Override
    public String rejectionReason(NotificationRequest request) {
        if (!request.recipient().matches("\\+7\\d{10}")) {
            return "номер должен быть в формате +7XXXXXXXXXX: '" + request.recipient() + "'";
        }
        return "SMS длиннее " + MAX_LENGTH + " символов (" + request.message().length() + ")";
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        return NotificationResult.sent(type(), request.recipient(), "SMS передано оператору");
    }
}
