import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Склад. Map по инвентарному коду даёт уникальность ключа и доступ за O(1);
 * наружу отдаются только копии или неизменяемые представления коллекций.
 */
public class Warehouse {

    /** Индивидуальный вариант 1: минимальный (неснижаемый) резерв = 10 единиц каждой позиции при выдаче. */
    public static final int MIN_RESERVE = 10;

    private final Map<String, StockItem> items = new LinkedHashMap<>();
    private final List<StockMovement> movements = new ArrayList<>();

    public void register(StockItem item) {
        if (items.putIfAbsent(item.getCode(), item) != null) {
            throw new IllegalArgumentException("позиция с кодом " + item.getCode() + " уже существует");
        }
    }

    public void receive(String code, int quantity) {
        StockItem item = find(code);
        item.increase(quantity);
        movements.add(new StockMovement(item.getCode(), MovementType.RECEIPT, quantity));
    }

    public void issue(String code, int quantity) {
        StockItem item = find(code);
        item.decrease(quantity, MIN_RESERVE);
        movements.add(new StockMovement(item.getCode(), MovementType.ISSUE, quantity));
    }

    public StockItem find(String code) {
        StockItem item = code == null ? null : items.get(code.trim());
        if (item == null) {
            throw new IllegalArgumentException("позиция с кодом " + code + " не найдена");
        }
        return item;
    }

    /** Новый список: его изменение не затрагивает склад. */
    public List<StockItem> findByCategory(Category category) {
        List<StockItem> result = new ArrayList<>();
        for (StockItem item : items.values()) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    /** Сводка остатков по категориям; пересчитывается по текущим карточкам и возвращается новой Map. */
    public Map<Category, Integer> totalsByCategory() {
        Map<Category, Integer> totals = new EnumMap<>(Category.class);
        for (StockItem item : items.values()) {
            totals.merge(item.getCategory(), item.getQuantity(), Integer::sum);
        }
        return totals;
    }

    /** Уникальный перечень категорий, в которых есть хотя бы одна позиция. */
    public Set<Category> usedCategories() {
        Set<Category> result = EnumSet.noneOf(Category.class);
        for (StockItem item : items.values()) {
            result.add(item.getCategory());
        }
        return result;
    }

    public List<StockMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

    public int size() {
        return items.size();
    }
}
