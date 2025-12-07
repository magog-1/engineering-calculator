package com.engineeringcalc;

import com.engineeringcalc.calculator.OhmsLawCalculator;
import com.engineeringcalc.calculator.VoltageDividerCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс для тестирования калькулятора электротехнических расчетов.
 * Содержит модульные тесты для проверки корректности вычислений
 * по закону Ома и делителю напряжения.
 *
 * @author Your Name
 * @version 1.0
 */
public class EngineeringCalculatorTest {

    @Test
    @DisplayName("Тест закона Ома: расчет напряжения U = I * R")
    public void testOhmsLawVoltage() {
        // Arrange
        double current = 2.0; // Ампер
        double resistance = 10.0; // Ом
        double expected = 20.0; // Вольт

        // Act
        double actual = OhmsLawCalculator.calculateVoltage(current, resistance);

        // Assert
        assertEquals(expected, actual, 0.001,
                "Напряжение должно быть равно 20В при токе 2А и сопротивлении 10Ом");
    }

    @Test
    @DisplayName("Тест закона Ома: расчет тока I = U / R")
    public void testOhmsLawCurrent() {
        // Arrange
        double voltage = 24.0; // Вольт
        double resistance = 8.0; // Ом
        double expected = 3.0; // Ампер

        // Act
        double actual = OhmsLawCalculator.calculateCurrent(voltage, resistance);

        // Assert
        assertEquals(expected, actual, 0.001,
                "Ток должен быть равен 3А при напряжении 24В и сопротивлении 8Ом");
    }

    @Test
    @DisplayName("Тест закона Ома: расчет сопротивления R = U / I")
    public void testOhmsLawResistance() {
        // Arrange
        double voltage = 12.0; // Вольт
        double current = 0.5; // Ампер
        double expected = 24.0; // Ом

        // Act
        double actual = OhmsLawCalculator.calculateResistance(voltage, current);

        // Assert
        assertEquals(expected, actual, 0.001,
                "Сопротивление должно быть равно 24Ом при напряжении 12В и токе 0.5А");
    }

    @Test
    @DisplayName("Тест делителя напряжения: расчет выходного напряжения")
    public void testVoltageDivider() {
        // Arrange
        double inputVoltage = 12.0; // Вольт
        double r1 = 1000.0; // Ом
        double r2 = 2000.0; // Ом
        double expected = 8.0; // Вольт (Vout = Vin * R2 / (R1 + R2))

        // Act
        double actual = VoltageDividerCalculator.calculateOutputVoltage(inputVoltage, r1, r2);

        // Assert
        assertEquals(expected, actual, 0.001,
                "Выходное напряжение должно быть 8В");
    }

    @Test
    @DisplayName("Тест делителя напряжения: проверка граничных условий (R1=0)")
    public void testVoltageDividerZeroR1() {
        // Arrange
        double inputVoltage = 10.0;
        double r1 = 0.0;
        double r2 = 1000.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            VoltageDividerCalculator.calculateOutputVoltage(inputVoltage, r1, r2);
        }, "Должно выброситься исключение при R1 = 0");
    }

    @Test
    @DisplayName("Тест закона Ома: деление на ноль (R=0)")
    public void testOhmsLawDivisionByZero() {
        // Arrange
        double voltage = 12.0;
        double resistance = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            OhmsLawCalculator.calculateCurrent(voltage, resistance);
        }, "Должно выброситься исключение при делении на ноль");
    }
}

