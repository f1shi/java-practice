import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Заказ оборудования. Агрегат: позиции изменяются только через методы заказа.
 * Итог не хранится отдельным полем, а вычисляется из текущего состава.
 */
public class Order {

    /**
     * Индивидуальный вариант 3 — правило объединения повторного товара (модификатор 20):
     * повторно добавленный товар объединяется с существующей строкой, только если итоговое количество
     * не превышает 20; иначе операция отклоняется и строка не меняется.
     */
    public static final int MERGE_LIMIT = 20;

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
            checkMerge(existing, quantity);
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

    private static void checkMerge(OrderItem existing, int quantity) {
        OrderItem.checkQuantity(quantity);
        int merged = existing.getQuantity() + quantity;
        if (merged > MERGE_LIMIT) {
            throw new IllegalArgumentException("товар " + existing.getProduct().getId() + " уже есть в заказе ("
                    + existing.getQuantity() + " шт.); объединение до " + merged + " шт. превышает лимит "
                    + MERGE_LIMIT + " для повторного добавления");
        }
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
