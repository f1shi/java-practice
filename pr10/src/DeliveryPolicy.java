/** Общий контракт способа доставки: ограничения + формула стоимости. */
public interface DeliveryPolicy {

    /** Какой способ доставки реализует политика (используется фабрикой при регистрации). */
    DeliveryType type();

    /** Возможна ли доставка с такими параметрами. */
    boolean supports(DeliveryRequest request);

    /** Стоимость доставки в рублях. Вызывается только для поддерживаемых запросов. */
    double calculateCost(DeliveryRequest request);

    /** Человекочитаемое описание ограничений для сообщения об отказе. */
    String limitsDescription();
}
