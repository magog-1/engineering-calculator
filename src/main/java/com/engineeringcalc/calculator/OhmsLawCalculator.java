package com.engineeringcalc.calculator;

public class OhmsLawCalculator {

    public enum Parameter {
        VOLTAGE, CURRENT, RESISTANCE
    }

    public static class Result {
        public double voltage;
        public double current;
        public double resistance;
        public Parameter calculated;

        @Override
        public String toString() {
            return String.format("V=%.3f V, I=%.6f A, R=%.3f Ω (Calculated: %s)",
                    voltage, current, resistance, calculated);
        }
    }

    /**
     * Рассчитывает неизвестный параметр по закону Ома
     * @param v Напряжение (В), null если неизвестно
     * @param i Ток (А), null если неизвестно
     * @param r Сопротивление (Ом), null если неизвестно
     * @return Результат с вычисленным параметром
     */
    public static Result calculate(Double v, Double i, Double r) {
        Result result = new Result();

        int knownParams = 0;
        if (v != null) knownParams++;
        if (i != null) knownParams++;
        if (r != null) knownParams++;

        if (knownParams != 2) {
            throw new IllegalArgumentException(
                    "Необходимо указать ровно два параметра из трех");
        }

        if (v == null) {
            // V = I * R
            result.voltage = i * r;
            result.current = i;
            result.resistance = r;
            result.calculated = Parameter.VOLTAGE;
        } else if (i == null) {
            // I = V / R
            result.voltage = v;
            result.current = v / r;
            result.resistance = r;
            result.calculated = Parameter.CURRENT;
        } else {
            // R = V / I
            result.voltage = v;
            result.current = i;
            result.resistance = v / i;
            result.calculated = Parameter.RESISTANCE;
        }

        return result;
    }

    // Конвертация единиц
    public static double convertVoltage(double value, String unit) {
        return switch (unit) {
            case "В" -> value;
            case "мВ" -> value / 1000.0;
            default -> value;
        };
    }

    public static double convertCurrent(double value, String unit) {
        return switch (unit) {
            case "А" -> value;
            case "мА" -> value / 1000.0;
            default -> value;
        };
    }

    public static double convertResistance(double value, String unit) {
        return switch (unit) {
            case "Ом" -> value;
            case "кОм" -> value * 1000.0;
            case "МОм" -> value * 1000000.0;
            default -> value;
        };
    }

    /**
     * Рассчитывает напряжение по закону Ома.
     *
     * @param current сила тока в амперах (должна быть больше или равна 0)
     * @param resistance сопротивление в омах (должно быть больше 0)
     * @return напряжение в вольтах
     * @throws IllegalArgumentException если ток отрицательный или сопротивление меньше или равно 0
     *
     * @see #calculateCurrent(double, double)
     * @see #calculateResistance(double, double)
     */
    public static double calculateVoltage(double current, double resistance) {
        validatePositive(current, "Ток");
        validatePositive(resistance, "Сопротивление");

        return current * resistance;
    }

    /**
     * Рассчитывает силу тока по закону Ома.
     *
     * @param voltage напряжение в вольтах (должно быть больше или равно 0)
     * @param resistance сопротивление в омах (должно быть больше 0)
     * @return сила тока в амперах
     * @throws IllegalArgumentException если напряжение отрицательное или сопротивление равно 0
     *
     * @see #calculateVoltage(double, double)
     */
    public static double calculateCurrent(double voltage, double resistance) {
        validatePositive(voltage, "Напряжение");
        if (resistance == 0) {
            throw new IllegalArgumentException("Сопротивление не может быть равно нулю");
        }
        validatePositive(resistance, "Сопротивление");

        return voltage / resistance;
    }

    /**
     * Рассчитывает сопротивление по закону Ома.
     *
     * @param voltage напряжение в вольтах (должно быть больше или равно 0)
     * @param current сила тока в амперах (должна быть больше 0)
     * @return сопротивление в омах
     * @throws IllegalArgumentException если напряжение отрицательное или ток равен 0
     */
    public static double calculateResistance(double voltage, double current) {
        validatePositive(voltage, "Напряжение");
        if (current == 0) {
            throw new IllegalArgumentException("Ток не может быть равен нулю");
        }
        validatePositive(current, "Ток");

        return voltage / current;
    }

    /**
     * Валидирует, что значение положительное.
     *
     * @param value проверяемое значение
     * @param paramName название параметра для сообщения об ошибке
     * @throws IllegalArgumentException если значение отрицательное
     */
    private static void validatePositive(double value, String paramName) {
        if (value < 0) {
            throw new IllegalArgumentException(paramName + " не может быть отрицательным");
        }
    }
}