package com.engineeringcalc.calculator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки функциональности OhmsLawCalculator.
 *
 * @author magog-1
 * @version 1.0
 */
public class OhmsLawCalculatorTest {

    private static final double DELTA = 0.0001; // Точность сравнения

    /**
     * Тест расчета напряжения по закону Ома.
     * Проверяет вычисление V = I * R.
     */
    @Test
    public void testCalculateVoltage() {
        // Arrange
        Double voltage = null;
        Double current = 2.0; // 2 А
        Double resistance = 5.0; // 5 Ом

        // Act
        OhmsLawCalculator.Result result = OhmsLawCalculator.calculate(voltage, current, resistance);

        // Assert
        assertEquals(10.0, result.voltage, DELTA, "Напряжение должно быть 10 В");
        assertEquals(2.0, result.current, DELTA);
        assertEquals(5.0, result.resistance, DELTA);
        assertEquals(OhmsLawCalculator.Parameter.VOLTAGE, result.calculated,
                "Вычисленный параметр должен быть VOLTAGE");
    }

    /**
     * Тест расчета силы тока по закону Ома.
     * Проверяет вычисление I = V / R.
     */
    @Test
    public void testCalculateCurrent() {
        // Arrange
        Double voltage = 12.0; // 12 В
        Double current = null;
        Double resistance = 4.0; // 4 Ом

        // Act
        OhmsLawCalculator.Result result = OhmsLawCalculator.calculate(voltage, current, resistance);

        // Assert
        assertEquals(12.0, result.voltage, DELTA);
        assertEquals(3.0, result.current, DELTA, "Ток должен быть 3 А");
        assertEquals(4.0, result.resistance, DELTA);
        assertEquals(OhmsLawCalculator.Parameter.CURRENT, result.calculated,
                "Вычисленный параметр должен быть CURRENT");
    }

    /**
     * Тест расчета сопротивления по закону Ома.
     * Проверяет вычисление R = V / I.
     */
    @Test
    public void testCalculateResistance() {
        // Arrange
        Double voltage = 24.0; // 24 В
        Double current = 3.0; // 3 А
        Double resistance = null;

        // Act
        OhmsLawCalculator.Result result = OhmsLawCalculator.calculate(voltage, current, resistance);

        // Assert
        assertEquals(24.0, result.voltage, DELTA);
        assertEquals(3.0, result.current, DELTA);
        assertEquals(8.0, result.resistance, DELTA, "Сопротивление должно быть 8 Ом");
        assertEquals(OhmsLawCalculator.Parameter.RESISTANCE, result.calculated,
                "Вычисленный параметр должен быть RESISTANCE");
    }

    /**
     * Тест обработки исключения при передаче всех трех параметров.
     * Должно выбрасываться IllegalArgumentException.
     */
    @Test
    public void testCalculateWithAllThreeParameters() {
        // Arrange
        Double voltage = 10.0;
        Double current = 2.0;
        Double resistance = 5.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            OhmsLawCalculator.calculate(voltage, current, resistance);
        }, "Передача всех трех параметров должна выбрасывать исключение");
    }

    /**
     * Тест обработки исключения при передаче только одного параметра.
     * Должно выбрасываться IllegalArgumentException.
     */
    @Test
    public void testCalculateWithOnlyOneParameter() {
        // Arrange
        Double voltage = 10.0;
        Double current = null;
        Double resistance = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            OhmsLawCalculator.calculate(voltage, current, resistance);
        }, "Передача только одного параметра должна выбрасывать исключение");
    }

    /**
     * Тест конвертации напряжения из милливольт в вольты.
     */
    @Test
    public void testConvertVoltageFromMillivolts() {
        // Arrange
        double millivolts = 5000.0; // 5000 мВ

        // Act
        double volts = OhmsLawCalculator.convertVoltage(millivolts, "мВ");

        // Assert
        assertEquals(5.0, volts, DELTA, "5000 мВ должно быть равно 5 В");
    }

    /**
     * Тест конвертации тока из миллиампер в амперы.
     */
    @Test
    public void testConvertCurrentFromMilliamps() {
        // Arrange
        double milliamps = 1500.0; // 1500 мА

        // Act
        double amps = OhmsLawCalculator.convertCurrent(milliamps, "мА");

        // Assert
        assertEquals(1.5, amps, DELTA, "1500 мА должно быть равно 1.5 А");
    }

    /**
     * Тест конвертации сопротивления из килоом в омы.
     */
    @Test
    public void testConvertResistanceFromKiloohms() {
        // Arrange
        double kiloohms = 4.7; // 4.7 кОм

        // Act
        double ohms = OhmsLawCalculator.convertResistance(kiloohms, "кОм");

        // Assert
        assertEquals(4700.0, ohms, DELTA, "4.7 кОм должно быть равно 4700 Ом");
    }

    /**
     * Тест конвертации сопротивления из мегаом в омы.
     */
    @Test
    public void testConvertResistanceFromMegaohms() {
        // Arrange
        double megaohms = 2.2; // 2.2 МОм

        // Act
        double ohms = OhmsLawCalculator.convertResistance(megaohms, "МОм");

        // Assert
        assertEquals(2200000.0, ohms, DELTA, "2.2 МОм должно быть равно 2200000 Ом");
    }

    /**
     * Тест вычисления с малыми значениями.
     */
    @Test
    public void testCalculateWithSmallValues() {
        // Arrange
        Double voltage = 0.005; // 5 мВ
        Double current = 0.001; // 1 мА
        Double resistance = null;

        // Act
        OhmsLawCalculator.Result result = OhmsLawCalculator.calculate(voltage, current, resistance);

        // Assert
        assertEquals(5.0, result.resistance, DELTA, "Сопротивление должно быть 5 Ом");
    }
}
