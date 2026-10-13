/** Сервис расчёта. Не содержит ветвлений по конкретным способам доставки. */
public class DeliveryService {

    private final DeliveryPolicyFactory factory;

    public DeliveryService(DeliveryPolicyFactory factory) {
        this.factory = factory;
    }

    public double calculate(DeliveryRequest request) {
        DeliveryPolicy policy = factory.get(request.type());
        if (!policy.supports(request)) {
            throw new DeliveryNotPossibleException("доставка " + request.type() + " невозможна: "
                    + request.distanceKm() + " км, " + request.weightKg() + " кг; допустимо: "
                    + policy.limitsDescription());
        }
        return Math.round(policy.calculateCost(request) * 100.0) / 100.0;
    }
}
