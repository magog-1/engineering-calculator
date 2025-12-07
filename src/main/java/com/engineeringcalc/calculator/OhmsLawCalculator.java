package com.engineeringcalc.calculator;

/**
 * Калькулятор для вычислений по закону Ома.
 *
 * <p>Закон Ома устанавливает связь между напряжением (V), силой тока (I)
 * и сопротивлением (R) в электрической цепи:</p>
 * <pre>
 * V = I * R
 * I = V / R
 * R = V / I
 * </pre>
 *
 * <p>Класс предоставляет метод для расчета любого из трех параметров,
 * если известны два других, а также методы конвертации единиц измерения.</p>
 *
 * @author magog-1
 * @version 1.0
 * @since 2025-12-08
 */
public class OhmsLawCalculator {

    /**
     * Перечисление параметров электрической цепи.
     */
    public enum Parameter {
        /** Напряжение (вольты) */
        VOLTAGE,
        /** Сила тока (амперы) */
        CURRENT,
        /** Сопротивление (омы) */
        RESISTANCE
    }

    /**
     * Класс для хранения результатов вычислений по закону Ома.
     * Содержит все три параметра и указание, какой из них был вычислен.
     */
    public static class Result {
        /** Напряжение в вольтах */
        public double voltage;

        /** Сила тока в амперах */
        public double current;

        /** Сопротивление в омах */
        public double resistance;

        /** Вычисленный параметр */
        public Parameter calculated;

        /**
         * Возвращает строковое представление результата.
         *
         * @return форматированная строка с результатами вычислений
         */
        @Override
        public String toString() {
            return String.format("V=%.3f V, I=%.6f A, R=%.3f Ω (Calculated: %s)",
                    voltage, current, resistance, calculated);
        }
    }

    /**
     * Рассчитывает неизвестный параметр по закону Ома.
     *
     * <p>Требуется указать ровно два известных параметра из трех.
     * Третий параметр будет вычислен автоматически.</p>
     *
     * <p>Примеры использования:</p>
     * <pre>
     * // Найти напряжение, зная ток и сопротивление
     * Result r1 = OhmsLawCalculator.calculate(null, 2.0, 5.0);
     * // r1.voltage = 10.0 В
     *
     * // Найти ток, зная напряжение и сопротивление
     * Result r2 = OhmsLawCalculator.calculate(12.0, null, 4.0);
     * // r2.current = 3.0 А
     *
     * // Найти сопротивление, зная напряжение и ток
     * Result r3 = OhmsLawCalculator.calculate(24.0, 3.0, null);
     * // r3.resistance = 8.0 Ом
     * </pre>
     *
     * @param v напряжение в вольтах, null если неизвестно
     * @param i сила тока в амперах, null если неизвестно
     * @param r сопротивление в омах, null если неизвестно
     * @return объект Result с вычисленным параметром и остальными значениями
     * @throws IllegalArgumentException если количество известных параметров не равно 2
     *
     * @see Result
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

    /**
     * Конвертирует напряжение в базовую единицу измерения (вольты).
     *
     * <p>Поддерживаемые единицы измерения:</p>
     * <ul>
     *   <li>"В" - вольты (без преобразования)</li>
     *   <li>"мВ" - милливольты (делится на 1000)</li>
     * </ul>
     *
     * @param value значение напряжения
     * @param unit единица измерения ("В" или "мВ")
     * @return значение в вольтах
     */
    public static double convertVoltage(double value, String unit) {
        return switch (unit) {
            case "В" -> value;
            case "мВ" -> value / 1000.0;
            default -> value;
        };
    }

    /**
     * Конвертирует силу тока в базовую единицу измерения (амперы).
     *
     * <p>Поддерживаемые единицы измерения:</p>
     * <ul>
     *   <li>"А" - амперы (без преобразования)</li>
     *   <li>"мА" - миллиамперы (делится на 1000)</li>
     * </ul>
     *
     * @param value значение силы тока
     * @param unit единица измерения ("А" или "мА")
     * @return значение в амперах
     */
    public static double convertCurrent(double value, String unit) {
        return switch (unit) {
            case "А" -> value;
            case "мА" -> value / 1000.0;
            default -> value;
        };
    }

    /**
     * Конвертирует сопротивление в базовую единицу измерения (омы).
     *
     * <p>Поддерживаемые единицы измерения:</p>
     * <ul>
     *   <li>"Ом" - омы (без преобразования)</li>
     *   <li>"кОм" - килоомы (умножается на 1000)</li>
     *   <li>"МОм" - мегаомы (умножается на 1000000)</li>
     * </ul>
     *
     * @param value значение сопротивления
     * @param unit единица измерения ("Ом", "кОм" или "МОм")
     * @return значение в омах
     */
    public static double convertResistance(double value, String unit) {
        return switch (unit) {
            case "Ом" -> value;
            case "кОм" -> value * 1000.0;
            case "МОм" -> value * 1000000.0;
            default -> value;
        };
    }
}
