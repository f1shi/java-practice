package ru.practice.bonus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Исполняемая спецификация BonusCalculator. Каждый тест создаёт свои данные и не зависит от порядка запуска. */
class BonusCalculatorTest {

    private static final double SALARY = 100_000.0;
    private static final double DELTA = 0.001;

    private BonusCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new BonusCalculator();
    }

    private double bonus(double salary, double kpi, int rating, int years) {
        return calculator.calculate(new EmployeePerformance(salary, kpi, rating, years));
    }

    @Nested
    @DisplayName("Типовые случаи")
    class Typical {

        @Test
        @DisplayName("KPI 70 %, рейтинг 3, стаж 1 — базовые 10 %")
        void baseRate() {
            // Arrange — данные в аргументах; Act
            double result = bonus(SALARY, 70, 3, 1);
            // Assert
            assertEquals(10_000.0, result, DELTA);
        }

        @Test
        @DisplayName("KPI 70 %, рейтинг 5, стаж 4 — 10 + 5 + 2 = 17 %")
        void allBonusesCombined() {
            assertEquals(17_000.0, bonus(SALARY, 70, 5, 4), DELTA);
        }

        @Test
        @DisplayName("Нулевой оклад даёт нулевую премию")
        void zeroSalary() {
            assertEquals(0.0, bonus(0, 95, 5, 12), DELTA);
        }
    }

    @Nested
    @DisplayName("Граничные значения")
    class Boundaries {

        @Test
        @DisplayName("D1: KPI 80.00 — повышенный коэффициент 20 %")
        void kpiExactly80IsHighRate() {
            assertEquals(20_000.0, bonus(SALARY, 80.00, 3, 0), DELTA);
        }

        @Test
        @DisplayName("KPI 79.99 — базовый коэффициент 10 %")
        void kpi7999IsBaseRate() {
            assertEquals(10_000.0, bonus(SALARY, 79.99, 3, 0), DELTA);
        }

        @Test
        @DisplayName("KPI 50.00 — премия есть, 49.99 — нет")
        void kpiMinimumBoundary() {
            assertEquals(10_000.0, bonus(SALARY, 50.00, 3, 0), DELTA);
            assertEquals(0.0, bonus(SALARY, 49.99, 3, 0), DELTA);
        }

        @Test
        @DisplayName("Вариант 3: ставка ровно 20 % не урезается, 22 % и 30 % ограничиваются до 20 %")
        void maxBonusCap() {
            assertEquals(20_000.0, bonus(SALARY, 80, 3, 0), DELTA);   // 20 % — на границе
            assertEquals(20_000.0, bonus(SALARY, 79.99, 5, 10), DELTA); // 10 + 5 + 5 = 20 %
            assertEquals(20_000.0, bonus(SALARY, 80, 3, 3), DELTA);   // 22 % -> 20 %
            assertEquals(20_000.0, bonus(SALARY, 95, 5, 12), DELTA);  // 30 % -> 20 %
        }

        @Test
        @DisplayName("D2: стаж ровно 10 лет — надбавка 5 п.п., а не 2")
        void veteranBonusAtTenYears() {
            assertEquals(15_000.0, bonus(SALARY, 70, 3, 10), DELTA);
        }

        @Test
        @DisplayName("Стаж 2 / 3 / 9 лет — 0 / 2 / 2 п.п.")
        void seniorityBoundaries() {
            assertEquals(10_000.0, bonus(SALARY, 70, 3, 2), DELTA);
            assertEquals(12_000.0, bonus(SALARY, 70, 3, 3), DELTA);
            assertEquals(12_000.0, bonus(SALARY, 70, 3, 9), DELTA);
        }

        @Test
        @DisplayName("D4: рейтинг 2 — премии нет, рейтинг 3 — есть")
        void ratingBoundary() {
            assertEquals(0.0, bonus(SALARY, 95, 2, 5), DELTA);
            assertEquals(12_000.0, bonus(SALARY, 70, 3, 5), DELTA);
        }

        @Test
        @DisplayName("D5: результат округлён до копеек")
        void roundedToKopecks() {
            assertEquals(40.00, bonus(333.33, 70, 3, 3), 0.0);
        }
    }

    @Nested
    @DisplayName("Некорректные значения")
    class InvalidInput {

        @Test
        @DisplayName("D6: отрицательный оклад отклоняется")
        void negativeSalary() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> new EmployeePerformance(-1, 70, 3, 1));
            assertEquals("оклад не может быть отрицательным: -1.0", e.getMessage());
        }

        @Test
        @DisplayName("Отрицательный KPI отклоняется")
        void negativeKpi() {
            assertThrows(IllegalArgumentException.class, () -> new EmployeePerformance(SALARY, -0.01, 3, 1));
        }

        @Test
        @DisplayName("Отрицательный стаж отклоняется")
        void negativeYears() {
            assertThrows(IllegalArgumentException.class, () -> new EmployeePerformance(SALARY, 70, 3, -1));
        }

        @ParameterizedTest(name = "рейтинг {0} вне диапазона 1..5")
        @CsvSource({"0", "6", "-3"})
        void ratingOutOfRange(int rating) {
            assertThrows(IllegalArgumentException.class, () -> new EmployeePerformance(SALARY, 70, rating, 1));
        }

        @Test
        @DisplayName("D3: null вместо показателей — IllegalArgumentException, а не NPE")
        void nullPerformance() {
            assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
        }
    }

    @ParameterizedTest(name = "оклад {0}, KPI {1}, рейтинг {2}, стаж {3} -> {4}")
    @DisplayName("Таблица правил расчёта")
    @CsvSource({
            "100000, 49.99, 5, 20,     0.00",
            "100000, 50.00, 3,  0, 10000.00",
            "100000, 79.99, 4,  2, 10000.00",
            "100000, 80.00, 4,  2, 20000.00",
            "100000, 80.00, 5,  3, 20000.00",
            "100000, 150.0, 5, 10, 20000.00",
            " 55555, 85.00, 1, 15,     0.00",
            " 55555, 85.00, 3, 15, 11111.00",
            " 55555, 70.00, 4, 15,  8333.25"
    })
    void rulesTable(double salary, double kpi, int rating, int years, double expected) {
        assertEquals(expected, bonus(salary, kpi, rating, years), DELTA);
    }
}
