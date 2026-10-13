/** Самовывоз: бесплатно, вес до 50 кг, расстояние не важно. */
public class PickupPolicy implements DeliveryPolicy {

    static final double MAX_WEIGHT_KG = 50;

    @Override
    public DeliveryType type() {
        return DeliveryType.PICKUP;
    }

    @Override
    public boolean supports(DeliveryRequest request) {
        return request.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest request) {
        return 0.0;
    }

    @Override
    public String limitsDescription() {
        return "вес до " + MAX_WEIGHT_KG + " кг";
    }
}
