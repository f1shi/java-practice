/** Позиция заказа: товар и количество. Изменяется только через Order. */
public class OrderItem {

    /** Допустимое количество одной позиции: от 1 до 100. */
    public static final int MAX_QUANTITY = 100;
    public static final int MIN_QUANTITY = 1;

    private final Product product;
    private int quantity;

    OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("товар не указан");
        }
        checkQuantity(quantity);
        this.product = product;
        this.quantity = quantity;
    }

    /** Объединение повторного товара: итоговое количество тоже должно быть в допустимом диапазоне. */
    void increase(int delta) {
        checkQuantity(delta);
        int newQuantity = quantity + delta;
        if (newQuantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("после объединения количество товара " + product.getId()
                    + " составит " + newQuantity + ", максимум " + MAX_QUANTITY);
        }
        quantity = newQuantity;
    }

    static void checkQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("количество должно быть от " + MIN_QUANTITY + " до "
                    + MAX_QUANTITY + ", получено " + quantity);
        }
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return product + " x " + quantity;
    }
}
