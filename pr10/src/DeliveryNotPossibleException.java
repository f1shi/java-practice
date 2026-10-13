/** Доставка выбранным способом невозможна при данных параметрах. */
public class DeliveryNotPossibleException extends RuntimeException {
    public DeliveryNotPossibleException(String message) {
        super(message);
    }
}
