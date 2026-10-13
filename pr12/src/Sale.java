/** Продажа. Неизменяемая запись; сумма — неотрицательная. */
public record Sale(String id, String manager, String region, double amount) {
    public Sale {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id продажи не может быть пустым");
        }
        if (manager == null || manager.isBlank()) {
            throw new IllegalArgumentException("менеджер не указан для продажи " + id);
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("регион не указан для продажи " + id);
        }
        if (Double.isNaN(amount) || amount < 0) {
            throw new IllegalArgumentException("сумма продажи " + id + " некорректна: " + amount);
        }
    }

    public String getId() {
        return id;
    }

    public String getManager() {
        return manager;
    }

    public String getRegion() {
        return region;
    }

    public double getAmount() {
        return amount;
    }
}
