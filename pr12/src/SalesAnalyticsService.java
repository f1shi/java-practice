import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Аналитика по загруженному списку продаж. Ни один метод не изменяет исходный список
 * и не модифицирует внешние коллекции внутри stream.
 */
public class SalesAnalyticsService {

    /** Индивидуальный вариант 1: порог крупной сделки = 10 тыс. руб. (сделка с суммой >= 10 000 — крупная). */
    public static final double LARGE_DEAL_THRESHOLD = 10_000.0;

    /** Сортировка «лучшие сверху»: сумма по убыванию, при равенстве — менеджер по алфавиту, затем id. */
    public static final Comparator<Sale> BY_AMOUNT_DESC_THEN_MANAGER =
            Comparator.comparingDouble(Sale::getAmount).reversed()
                    .thenComparing(Sale::getManager)
                    .thenComparing(Sale::getId);

    public List<Sale> topSales(List<Sale> sales, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit не может быть отрицательным: " + limit);
        }
        return sales.stream()
                .filter(sale -> sale.getAmount() > 0)
                .sorted(BY_AMOUNT_DESC_THEN_MANAGER)
                .limit(limit)
                .toList();
    }

    /** Максимальная продажа; при равных суммах — та же, что первая в topSales (детерминированно). */
    public Optional<Sale> maxSale(List<Sale> sales) {
        return sales.stream().min(BY_AMOUNT_DESC_THEN_MANAGER);
    }

    /** Новый Map менеджер -> сумма продаж; TreeMap даёт стабильный алфавитный порядок. */
    public Map<String, Double> totalsByManager(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(Sale::getManager, TreeMap::new,
                        Collectors.summingDouble(Sale::getAmount)));
    }

    /** Средняя сумма продаж региона; 0.0, если в регионе нет продаж. */
    public double averageByRegion(List<Sale> sales, String region) {
        return sales.stream()
                .filter(sale -> sale.getRegion().equalsIgnoreCase(region))
                .mapToDouble(Sale::getAmount)
                .average()
                .orElse(0.0);
    }

    /** Императивная версия того же расчёта — оставлена для сравнения читаемости в отчёте. */
    public double averageByRegionLoop(List<Sale> sales, String region) {
        double sum = 0;
        int count = 0;
        for (Sale sale : sales) {
            if (sale.getRegion().equalsIgnoreCase(region)) {
                sum += sale.getAmount();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    /** Крупные сделки без побочных эффектов: результат собирается терминальной операцией. */
    public List<Sale> largeSales(List<Sale> sales) {
        return sales.stream()
                .filter(this::isLarge)
                .sorted(BY_AMOUNT_DESC_THEN_MANAGER)
                .toList();
    }

    public boolean isLarge(Sale sale) {
        return sale.getAmount() >= LARGE_DEAL_THRESHOLD;
    }
}
