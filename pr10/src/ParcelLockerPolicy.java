/**
 * Четвёртый способ — доставка в постамат (индивидуальный вариант 3: ограничение по весу = 20 кг).
 * Формула: 250 руб. + 10 руб. за каждый кг; вес до 20 кг включительно, расстояние до 30 км.
 */
public class ParcelLockerPolicy implements DeliveryPolicy {

    static final double BASE = 250;
    static final double PER_KG = 10;
    /** Индивидуальный вариант 3: ограничение по весу. */
    static final double MAX_WEIGHT_KG = 20;
    static final double MAX_DISTANCE_KM = 30;

    @Override
    public DeliveryType type() {
        return DeliveryType.PARCEL_LOCKER;
    }

    @Override
    public boolean supports(DeliveryRequest request) {
        return request.weightKg() <= MAX_WEIGHT_KG && request.distanceKm() <= MAX_DISTANCE_KM;
    }

    @Override
    public double calculateCost(DeliveryRequest request) {
        return BASE + PER_KG * request.weightKg();
    }

    @Override
    public String limitsDescription() {
        return "вес до " + MAX_WEIGHT_KG + " кг и расстояние до " + MAX_DISTANCE_KM + " км";
    }
}
