/** Стратегия канала: собственная проверка адресата и отправка. */
public interface NotificationChannel {

    ChannelType type();

    /** Поддерживает ли канал такого адресата и сообщение (валидация, специфичная для канала). */
    boolean supports(NotificationRequest request);

    /** Причина, по которой supports вернул false (для понятного отказа). */
    String rejectionReason(NotificationRequest request);

    NotificationResult send(NotificationRequest request);
}
