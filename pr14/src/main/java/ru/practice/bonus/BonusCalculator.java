package ru.practice.bonus;

/**
 * Расчёт премии. Правила:
 * <ul>
 *   <li>KPI &lt; KPI_MIN (60 %) — премии нет; KPI_MIN ≤ KPI &lt; 80 % — 10 % оклада; KPI ≥ 80 % — 20 % оклада;</li>
 *   <li>рейтинг 1–2 — премии нет независимо от KPI; рейтинг 5 — плюс 5 п.п.;</li>
 *   <li>стаж от 3 лет — плюс 2 п.п., от 10 лет — плюс 5 п.п. (надбавки не суммируются);</li>
 *   <li>премия округляется до копеек.</li>
 * </ul>
 */
public class BonusCalculator {

    /** Индивидуальный вариант 1: граница KPI сдвинута на 10 п.п. — минимальный KPI для премии 60 % (было 50 %). */
    static final double KPI_MIN = 60.0;
    static final double KPI_HIGH = 80.0;
    static final double BASE_RATE = 10.0;
    static final double HIGH_RATE = 20.0;
    static final int MIN_RATING_FOR_BONUS = 3;
    static final double TOP_RATING_BONUS = 5.0;
    static final double SENIOR_BONUS = 2.0;
    static final double VETERAN_BONUS = 5.0;

    public double calculate(EmployeePerformance p) {
        if (p == null) {
            throw new IllegalArgumentException("показатели сотрудника не переданы");
        }
        if (p.getKpiPercent() < KPI_MIN || p.getRating() < MIN_RATING_FOR_BONUS) {
            return 0.0;
        }
        double ratePercent = p.getKpiPercent() >= KPI_HIGH ? HIGH_RATE : BASE_RATE;
        if (p.getRating() == 5) {
            ratePercent += TOP_RATING_BONUS;
        }
        ratePercent += seniorityBonus(p.getYears());
        return Math.round(p.getSalary() * ratePercent) / 100.0;
    }

    private static double seniorityBonus(int years) {
        if (years >= 10) {
            return VETERAN_BONUS;
        }
        if (years >= 3) {
            return SENIOR_BONUS;
        }
        return 0.0;
    }
}
