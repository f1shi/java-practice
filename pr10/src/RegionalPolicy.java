/**
 * Четвёртый способ — региональная доставка транспортной компанией (индивидуальный вариант 1:
 * стоимость километра = 10 руб.). Формула: 200 руб. + 10 руб./км; расстояние 20..1000 км, вес до 100 кг.
 */
public class RegionalPolicy implements DeliveryPolicy {

    static final double BASE = 200;
    /** Индивидуальный вариант 1: стоимость километра. */
    static final double PER_KM = 10;
    static final double MIN_DISTANCE_KM = 20;
    static final double MAX_DISTANCE_KM = 1000;
    static final double MAX_WEIGHT_KG = 100;

    @Override
    public DeliveryType type() {
        return DeliveryType.REGIONAL;
    }

    @Override
    public boolean supports(DeliveryRequest request) {
        return request.distanceKm() >= MIN_DISTANCE_KM
                && request.distanceKm() <= MAX_DISTANCE_KM
                && request.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest request) {
        return BASE + PER_KM * request.distanceKm();
    }

    @Override
    public String limitsDescription() {
        return "расстояние от " + MIN_DISTANCE_KM + " до " + MAX_DISTANCE_KM + " км и вес до " + MAX_WEIGHT_KG + " кг";
    }
}
