/** Параметры доставки. Проверка корректности значений выполняется при создании. */
public record DeliveryRequest(DeliveryType type, double distanceKm, double weightKg) {
    public DeliveryRequest {
        if (type == null) {
            throw new IllegalArgumentException("способ доставки не указан");
        }
        if (Double.isNaN(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException("расстояние не может быть отрицательным: " + distanceKm);
        }
        if (Double.isNaN(weightKg) || weightKg <= 0) {
            throw new IllegalArgumentException("вес должен быть положительным: " + weightKg);
        }
    }
}
