import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Заказ оборудования. Агрегат: позиции изменяются только через методы заказа.
 * Итог не хранится отдельным полем, а вычисляется из текущего состава.
 */
public class Order {

    private final Employee employee;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("заказ должен принадлежать сотруднику");
        }
        this.employee = employee;
    }

    public void addProduct(Product product, int quantity) {
        ensureDraft();
        if (product == null) {
            throw new IllegalArgumentException("товар не указан");
        }
        OrderItem existing = findItem(product.getId());
        if (existing != null) {
            existing.increase(quantity);           // один товар — одна строка
        } else {
            items.add(new OrderItem(product, quantity));
        }
    }

    public void removeProduct(String productId) {
        ensureDraft();
        OrderItem existing = findItem(productId);
        if (existing == null) {
            throw new IllegalArgumentException("товара " + productId + " нет в заказе");
        }
        items.remove(existing);
    }

    public double total() {
        double sum = 0;
        for (OrderItem item : items) {
            sum += item.getLineTotal();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public void confirm() {
        ensureDraft();
        if (items.isEmpty()) {
            throw new IllegalStateException("нельзя подтвердить пустой заказ");
        }
        status = OrderStatus.CONFIRMED;
    }

    private void ensureDraft() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("заказ подтверждён и не может быть изменён");
        }
    }

    private OrderItem findItem(String productId) {
        for (OrderItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    public Employee getEmployee() {
        return employee;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /** Только чтение: внешний код не может обойти проверки addProduct/removeProduct. */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
