/** Экспресс: (500 руб. + 40 руб./км) с минимумом 800 руб.; до 50 км и 10 кг. */
public class ExpressPolicy implements DeliveryPolicy {

    static final double BASE = 500;
    static final double PER_KM = 40;
    static final double MIN_COST = 800;
    static final double MAX_DISTANCE_KM = 50;
    static final double MAX_WEIGHT_KG = 10;

    @Override
    public DeliveryType type() {
        return DeliveryType.EXPRESS;
    }

    @Override
    public boolean supports(DeliveryRequest request) {
        return request.distanceKm() <= MAX_DISTANCE_KM && request.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest request) {
        return Math.max(MIN_COST, BASE + PER_KM * request.distanceKm());
    }

    @Override
    public String limitsDescription() {
        return "расстояние до " + MAX_DISTANCE_KM + " км и вес до " + MAX_WEIGHT_KG + " кг";
    }
}
