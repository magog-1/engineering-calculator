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
}