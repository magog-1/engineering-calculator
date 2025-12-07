package com.engineeringcalc.calculator;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки функциональности VoltageDividerCalculator.
 *
 * @author magog-1
 * @version 1.0
 */
public class VoltageDividerCalculatorTest {

    private static final double DELTA = 0.01;

    /**
     * Тест поиска решений делителя с равными резисторами.
     * При R1 = R2 выходное напряжение должно быть половиной входного.
     */
    @Test
    public void testFindSolutionsForHalfVoltage() {
        // Arrange
        double vin = 10.0; // 10 В
        double vRequired = 5.0; // 5 В (половина входного)
        double tolerance = 5.0; // 5% допуск
        String series = "E12";
        double rMin = 100.0;
        double rMax = 10000.0;

        // Act
        List<VoltageDividerCalculator.DividerSolution> solutions =
                VoltageDividerCalculator.findSolutions(vin, vRequired, tolerance, series, rMin, rMax);

        // Assert
        assertFalse(solutions.isEmpty(), "Должны быть найдены решения");

        // Проверяем первое (лучшее) решение
        VoltageDividerCalculator.DividerSolution best = solutions.get(0);
        assertEquals(5.0, best.vout, tolerance / 100 * vRequired,
                "Выходное напряжение должно быть близко к 5 В");
        assertTrue(best.error <= tolerance,
                "Погрешность должна быть в пределах допуска");
    }

    /**
     * Тест поиска решений делителя для 1/3 напряжения.
     */
    @Test
    public void testFindSolutionsForOneThirdVoltage() {
        // Arrange
        double vin = 12.0; // 12 В
        double vRequired = 4.0; // 4 В (треть входного)
        double tolerance = 10.0; // 10% допуск
        String series = "E6";
        double rMin = 1000.0;
        double rMax = 100000.0;

        // Act
        List<VoltageDividerCalculator.DividerSolution> solutions =
                VoltageDividerCalculator.findSolutions(vin, vRequired, tolerance, series, rMin, rMax);

        // Assert
        assertFalse(solutions.isEmpty(), "Должны быть найдены решения");

        VoltageDividerCalculator.DividerSolution best = solutions.get(0);
        assertTrue(best.vout >= 3.6 && best.vout <= 4.4,
                "Выходное напряжение должно быть в пределах 3.6-4.4 В");
    }

    /**
     * Тест проверки структуры решения DividerSolution.
     */
    @Test
    public void testDividerSolutionStructure() {
        // Arrange
        double vin = 9.0;
        double vRequired = 3.0;
        double tolerance = 5.0;

        // Act
        List<VoltageDividerCalculator.DividerSolution> solutions =
                VoltageDividerCalculator.findSolutions(vin, vRequired, tolerance, "E12", 100, 10000);

        // Assert
        assertFalse(solutions.isEmpty());

        VoltageDividerCalculator.DividerSolution sol = solutions.get(0);
        assertNotNull(sol.r1Values, "r1Values не должно быть null");
        assertNotNull(sol.r2Values, "r2Values не должно быть null");
        assertNotNull(sol.r1Config, "r1Config не должно быть null");
        assertNotNull(sol.r2Config, "r2Config не должно быть null");
        assertTrue(sol.vout > 0, "Выходное напряжение должно быть положительным");
        assertTrue(sol.power > 0, "Мощность должна быть положительной");
    }

    /**
     * Тест методов getR1 и getR2 для последовательного соединения.
     */
    @Test
    public void testGetResistanceForSeriesConfiguration() {
        // Arrange
        VoltageDividerCalculator.DividerSolution sol = new VoltageDividerCalculator.DividerSolution();
        sol.r1Values = List.of(1000.0, 2000.0); // 1к + 2к = 3к
        sol.r1Config = "series";
        sol.r2Values = List.of(500.0, 1500.0); // 500 + 1.5к = 2к
        sol.r2Config = "series";

        // Act
        double r1 = sol.getR1();
        double r2 = sol.getR2();

        // Assert
        assertEquals(3000.0, r1, DELTA, "R1 должно быть 3000 Ом");
        assertEquals(2000.0, r2, DELTA, "R2 должно быть 2000 Ом");
    }

    /**
     * Тест методов getR1 и getR2 для параллельного соединения.
     */
    @Test
    public void testGetResistanceForParallelConfiguration() {
        // Arrange
        VoltageDividerCalculator.DividerSolution sol = new VoltageDividerCalculator.DividerSolution();
        sol.r1Values = List.of(2000.0, 2000.0); // 2к || 2к = 1к
        sol.r1Config = "parallel";
        sol.r2Values = List.of(3000.0); // 3к
        sol.r2Config = "series";

        // Act
        double r1 = sol.getR1();
        double r2 = sol.getR2();

        // Assert
        assertEquals(1000.0, r1, DELTA, "R1 параллельно должно быть 1000 Ом");
        assertEquals(3000.0, r2, DELTA, "R2 должно быть 3000 Ом");
    }

    /**
     * Тест ограничения количества решений (максимум 10).
     */
    @Test
    public void testSolutionsLimitedToTen() {
        // Arrange
        double vin = 10.0;
        double vRequired = 5.0;
        double tolerance = 50.0; // Большой допуск для множества решений

        // Act
        List<VoltageDividerCalculator.DividerSolution> solutions =
                VoltageDividerCalculator.findSolutions(vin, vRequired, tolerance, "E24", 100, 100000);

        // Assert
        assertTrue(solutions.size() <= 10,
                "Количество решений не должно превышать 10");
    }

    /**
     * Тест сортировки решений по погрешности.
     */
    @Test
    public void testSolutionsSortedByError() {
        // Arrange
        double vin = 12.0;
        double vRequired = 3.3;
        double tolerance = 15.0;

        // Act
        List<VoltageDividerCalculator.DividerSolution> solutions =
                VoltageDividerCalculator.findSolutions(vin, vRequired, tolerance, "E12", 1000, 50000);

        // Assert
        assertFalse(solutions.isEmpty());

        // Проверяем, что решения отсортированы по погрешности
        for (int i = 0; i < solutions.size() - 1; i++) {
            double error1 = Math.abs(solutions.get(i).error);
            double error2 = Math.abs(solutions.get(i + 1).error);
            assertTrue(error1 <= error2,
                    "Решения должны быть отсортированы по возрастанию погрешности");
        }
    }
}
