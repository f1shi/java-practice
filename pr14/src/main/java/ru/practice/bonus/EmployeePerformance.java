package ru.practice.bonus;

/** Показатели сотрудника за период. Некорректные значения отклоняются при создании. */
public class EmployeePerformance {

    private final double salary;
    private final double kpiPercent;
    private final int rating;
    private final int years;

    public EmployeePerformance(double salary, double kpiPercent, int rating, int years) {
        if (Double.isNaN(salary) || salary < 0) {
            throw new IllegalArgumentException("оклад не может быть отрицательным: " + salary);
        }
        if (Double.isNaN(kpiPercent) || kpiPercent < 0) {
            throw new IllegalArgumentException("KPI не может быть отрицательным: " + kpiPercent);
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("рейтинг должен быть от 1 до 5: " + rating);
        }
        if (years < 0) {
            throw new IllegalArgumentException("стаж не может быть отрицательным: " + years);
        }
        this.salary = salary;
        this.kpiPercent = kpiPercent;
        this.rating = rating;
        this.years = years;
    }

    public double getSalary() {
        return salary;
    }

    public double getKpiPercent() {
        return kpiPercent;
    }

    public int getRating() {
        return rating;
    }

    public int getYears() {
        return years;
    }
}
