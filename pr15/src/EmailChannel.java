/** Email: адрес с одним '@', непустым именем и доменом с точкой. */
public class EmailChannel implements NotificationChannel {

    @Override
    public ChannelType type() {
        return ChannelType.EMAIL;
    }

    @Override
    public boolean supports(NotificationRequest request) {
        String address = request.recipient();
        int at = address.indexOf('@');
        if (at <= 0 || at != address.lastIndexOf('@')) {
            return false;
        }
        String domain = address.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        return dot > 0 && dot < domain.length() - 1;
    }

    @Override
    public String rejectionReason(NotificationRequest request) {
        return "некорректный email-адрес '" + request.recipient() + "'";
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        return NotificationResult.sent(type(), request.recipient(), "письмо поставлено в очередь SMTP");
    }
}
