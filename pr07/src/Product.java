import java.util.Objects;

/** Товар каталога. Неизменяемый объект; равенство определяется уникальным id. */
public final class Product {
    private final String id;
    private final String name;
    private final double price;

    public Product(String id, String name, double price) {
        this.id = Validation.requireText(id, "id товара");
        this.name = Validation.requireText(name, "наименование товара");
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) {
            throw new IllegalArgumentException("цена товара " + id + " должна быть положительной: " + price);
        }
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Product other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " [" + id + "]";
    }
}
