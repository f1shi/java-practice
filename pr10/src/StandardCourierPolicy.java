/** Стандартный курьер: 300 руб. + 20 руб./км + 15 руб. за каждый кг сверх 5 кг; до 100 км и 30 кг. */
public class StandardCourierPolicy implements DeliveryPolicy {

    static final double BASE = 300;
    static final double PER_KM = 20;
    static final double FREE_WEIGHT_KG = 5;
    static final double PER_EXTRA_KG = 15;
    static final double MAX_DISTANCE_KM = 100;
    static final double MAX_WEIGHT_KG = 30;

    @Override
    public DeliveryType type() {
        return DeliveryType.STANDARD;
    }

    @Override
    public boolean supports(DeliveryRequest request) {
        return request.distanceKm() <= MAX_DISTANCE_KM && request.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest request) {
        double extraWeight = Math.max(0, request.weightKg() - FREE_WEIGHT_KG);
        return BASE + PER_KM * request.distanceKm() + PER_EXTRA_KG * extraWeight;
    }

    @Override
    public String limitsDescription() {
        return "расстояние до " + MAX_DISTANCE_KM + " км и вес до " + MAX_WEIGHT_KG + " кг";
    }
}
