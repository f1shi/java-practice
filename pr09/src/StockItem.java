import java.util.Objects;

/** Карточка позиции склада. Идентичность определяется инвентарным кодом. */
public class StockItem {

    private final String code;
    private final String name;
    private final Category category;
    private int quantity;

    public StockItem(String code, String name, Category category) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("инвентарный код не может быть пустым");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("наименование не может быть пустым");
        }
        if (category == null) {
            throw new IllegalArgumentException("категория не указана");
        }
        this.code = code.trim();
        this.name = name.trim();
        this.category = category;
    }

    void increase(int amount) {
        requirePositive(amount);
        quantity += amount;
    }

    /** Уменьшение остатка. Нижний предел (резерв) задаёт склад; сама карточка не допускает отрицательного остатка. */
    void decrease(int amount, int minRemaining) {
        requirePositive(amount);
        if (quantity - amount < minRemaining) {
            throw new IllegalStateException("недостаточно остатка по " + code + ": на складе " + quantity
                    + ", запрошено " + amount + ", неснижаемый резерв " + minRemaining);
        }
        quantity -= amount;
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("количество операции должно быть положительным: " + amount);
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StockItem other && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " " + name + " (" + category + "): " + quantity;
    }
}
