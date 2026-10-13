import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Самопроверка аналитического сервиса. */
public class SelfCheck {

    private static final SalesAnalyticsService S = new SalesAnalyticsService();
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        List<Sale> sales = List.of(
                new Sale("1", "Иванов", "Москва", 15_000),
                new Sale("2", "Петрова", "Казань", 8_200),
                new Sale("3", "Иванов", "Москва", 4_300),
                new Sale("4", "Сидоров", "Новосибирск", 27_500),
                new Sale("5", "Петрова", "Москва", 10_000),
                new Sale("6", "Сидоров", "Казань", 9_999.99),
                new Sale("7", "Абрамов", "Москва", 27_500),
                new Sale("8", "Петрова", "Новосибирск", 0));
        List<Sale> mutable = new ArrayList<>(sales);
        List<Sale> snapshot = List.copyOf(mutable);

        List<Sale> top = S.topSales(mutable, 3);
        check("top3 по сумме, при равенстве 27 500 — Абрамов раньше Сидорова",
                ids(top).equals(List.of("7", "4", "1")));
        check("topSales(0) — пустой список", S.topSales(mutable, 0).isEmpty());
        check("topSales(100) — все ненулевые (7)", S.topSales(mutable, 100).size() == 7);
        check("нулевая продажа не попадает в топ", !ids(S.topSales(mutable, 100)).contains("8"));
        expect("отрицательный limit", () -> S.topSales(mutable, -1));

        check("maxSale — сумма 27 500", S.maxSale(mutable).orElseThrow().getAmount() == 27_500);
        check("maxSale пустого списка — Optional.empty", S.maxSale(List.of()).isEmpty());

        Map<String, Double> totals = S.totalsByManager(mutable);
        check("итоги: Иванов 19 300, Петрова 18 200", totals.get("Иванов") == 19_300 && totals.get("Петрова") == 18_200);
        check("итоги — новый Map, ключи по алфавиту", List.copyOf(totals.keySet())
                .equals(List.of("Абрамов", "Иванов", "Петрова", "Сидоров")));
        totals.clear();
        check("очистка результата не влияет на повторный расчёт", S.totalsByManager(mutable).size() == 4);

        check("среднее Москва = (15000+4300+10000+27500)/4 = 14200", S.averageByRegion(mutable, "Москва") == 14_200);
        check("регистр региона не важен", S.averageByRegion(mutable, "москва") == 14_200);
        check("регион без продаж -> 0.0", S.averageByRegion(mutable, "Тверь") == 0.0);
        check("цикл и stream дают одинаковый результат",
                S.averageByRegion(mutable, "Казань") == S.averageByRegionLoop(mutable, "Казань"));

        // вариант 3: доля выручки 20 % крупнейших сделок. 8 сделок -> 2 верхние: 27 500 + 27 500 = 55 000 из 102 499.99
        check("доля top-20 % = 53.66 %", S.topDealsRevenueShare(mutable) == 53.66);
        check("1 сделка -> верхняя группа из 1 сделки = 100 %",
                S.topDealsRevenueShare(List.of(new Sale("a", "m", "r", 500))) == 100.0);
        List<Sale> five = List.of(new Sale("a", "m", "r", 50), new Sale("b", "m", "r", 20), new Sale("c", "m", "r", 10),
                new Sale("d", "m", "r", 10), new Sale("e", "m", "r", 10));
        check("5 сделок -> ровно 1 верхняя (50 из 100) = 50 %", S.topDealsRevenueShare(five) == 50.0);
        List<Sale> six = new ArrayList<>(five);
        six.add(new Sale("f", "m", "r", 0));
        check("6 сделок -> 2 верхние (округление вверх): 70 %", S.topDealsRevenueShare(six) == 70.0);
        check("пустой список -> 0.0", S.topDealsRevenueShare(List.of()) == 0.0);
        check("только нулевые суммы -> 0.0", S.topDealsRevenueShare(List.of(new Sale("z", "m", "r", 0))) == 0.0);
        // порог крупной сделки 10 000 включительно
        check("10 000 — крупная (граница включается)", S.isLarge(new Sale("x", "m", "r", 10_000)));
        check("9 999.99 — не крупная", !S.isLarge(new Sale("x", "m", "r", 9_999.99)));
        check("крупных сделок 4", S.largeSales(mutable).size() == 4);
        check("повторный вызов даёт тот же результат (нет накопления во внешнем списке)",
                S.largeSales(mutable).size() == 4);

        check("исходный список не изменён (порядок и состав)", mutable.equals(snapshot));
        expect("отрицательная сумма", () -> new Sale("x", "m", "r", -1));
        expect("пустой менеджер", () -> new Sale("x", " ", "r", 1));

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static List<String> ids(List<Sale> list) {
        return list.stream().map(Sale::getId).toList();
    }

    private static void expect(String name, Runnable action) {
        try {
            action.run();
            check(name + " -> ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check(name + " -> " + e.getMessage(), true);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
    }
}
