/** Сервисный слой: создание заказа и проверка перед подтверждением. */
public class OrderService {

    public Order createOrder(Employee employee) {
        return new Order(employee);
    }

    /** Проверки уровня сценария, выполняемые до перевода заказа в CONFIRMED. */
    public void validateForConfirm(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("заказ не указан");
        }
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("заказ уже подтверждён");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("заказ не содержит позиций");
        }
    }

    public void confirm(Order order) {
        validateForConfirm(order);
        order.confirm();
    }
}
