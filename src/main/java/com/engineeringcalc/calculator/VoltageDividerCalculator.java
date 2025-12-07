package com.engineeringcalc.calculator;

import java.util.*;

/**
 * Калькулятор для расчета и подбора делителя напряжения.
 *
 * <p>Делитель напряжения - это простая электрическая схема,
 * состоящая из двух последовательно соединенных резисторов,
 * которая преобразует большое напряжение в меньшее.</p>
 *
 * <p>Основная формула делителя напряжения:</p>
 * <pre>
 * Vout = Vin × R2 / (R1 + R2)
 * </pre>
 *
 * <p>Класс предоставляет интеллектуальный подбор номиналов резисторов
 * из стандартных рядов E6, E12 и E24. Поддерживает поиск решений с
 * использованием 2, 3 или 4 резисторов в различных конфигурациях
 * (последовательное и параллельное соединение).</p>
 *
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Подбор резисторов из стандартных рядов E6, E12, E24</li>
 *   <li>Поддержка последовательного и параллельного соединения</li>
 *   <li>Расчет погрешности для каждого решения</li>
 *   <li>Расчет потребляемой мощности делителя</li>
 *   <li>Автоматическая сортировка решений по качеству</li>
 * </ul>
 *
 * <p>Пример использования:</p>
 * <pre>
 * // Найти решения для получения 5В из 12В
 * List&lt;DividerSolution&gt; solutions = VoltageDividerCalculator.findSolutions(
 *     12.0,    // входное напряжение (В)
 *     5.0,     // требуемое выходное напряжение (В)
 *     5.0,     // допустимая погрешность (%)
 *     "E12",   // ряд резисторов
 *     100.0,   // минимальное сопротивление (Ом)
 *     100000.0 // максимальное сопротивление (Ом)
 * );
 *
 * // Получить лучшее решение
 * DividerSolution best = solutions.get(0);
 * System.out.println(best); // Выводит R1, R2, Vout, погрешность, мощность
 * </pre>
 *
 * @author magog-1
 * @version 1.0
 * @since 2025-12-08
 */
public class VoltageDividerCalculator {

    /**
     * Стандартные ряды номиналов резисторов по международным стандартам.
     *
     * <p>Доступные ряды:</p>
     * <ul>
     *   <li><b>E6</b> - 6 номиналов на декаду (допуск ±20%): 1.0, 1.5, 2.2, 3.3, 4.7, 6.8</li>
     *   <li><b>E12</b> - 12 номиналов на декаду (допуск ±10%): добавляются промежуточные значения</li>
     *   <li><b>E24</b> - 24 номинала на декаду (допуск ±5%): максимальная детализация</li>
     * </ul>
     *
     * <p>Значения умножаются на степени 10 для получения всех стандартных номиналов
     * (например: 1.0 Ом, 10 Ом, 100 Ом, 1 кОм, 10 кОм и т.д.).</p>
     */
    private static final Map<String, double[]> E_SERIES = Map.of(
            "E6", new double[]{1.0, 1.5, 2.2, 3.3, 4.7, 6.8},
            "E12", new double[]{1.0, 1.2, 1.5, 1.8, 2.2, 2.7, 3.3, 3.9, 4.7, 5.6, 6.8, 8.2},
            "E24", new double[]{1.0, 1.1, 1.2, 1.3, 1.5, 1.6, 1.8, 2.0, 2.2, 2.4, 2.7, 3.0,
                    3.3, 3.6, 3.9, 4.3, 4.7, 5.1, 5.6, 6.2, 6.8, 7.5, 8.2, 9.1}
    );

    /**
     * Класс для хранения одного решения делителя напряжения.
     *
     * <p>Содержит полную информацию о конфигурации делителя:
     * номиналы резисторов, способ их соединения, выходное напряжение,
     * погрешность и потребляемую мощность.</p>
     *
     * <p>Решение может состоять из 2, 3 или 4 резисторов, соединенных
     * последовательно (series) или параллельно (parallel).</p>
     */
    public static class DividerSolution {
        /**
         * Список номиналов резисторов верхнего плеча делителя (R1).
         * Может содержать один или несколько резисторов.
         */
        public List<Double> r1Values;

        /**
         * Список номиналов резисторов нижнего плеча делителя (R2).
         * Может содержать один или несколько резисторов.
         */
        public List<Double> r2Values;

        /**
         * Конфигурация верхнего плеча R1.
         * Значения: "series" (последовательное соединение) или
         * "parallel" (параллельное соединение).
         */
        public String r1Config;

        /**
         * Конфигурация нижнего плеча R2.
         * Значения: "series" (последовательное соединение) или
         * "parallel" (параллельное соединение).
         */
        public String r2Config;

        /**
         * Выходное напряжение делителя в вольтах.
         * Рассчитывается по формуле: Vout = Vin × R2 / (R1 + R2)
         */
        public double vout;

        /**
         * Погрешность в процентах относительно требуемого напряжения.
         * Рассчитывается как: |Vout - Vтреб| / Vтреб × 100%
         */
        public double error;

        /**
         * Потребляемая мощность делителя в ваттах.
         * Рассчитывается по формуле: P = Vin² / (R1 + R2)
         */
        public double power;

        /**
         * Вычисляет эффективное сопротивление верхнего плеча R1.
         *
         * <p>Для последовательного соединения: R = R1 + R2 + ... + Rn</p>
         * <p>Для параллельного соединения: 1/R = 1/R1 + 1/R2 + ... + 1/Rn</p>
         *
         * @return эффективное сопротивление R1 в омах
         */
        public double getR1() {
            if (r1Config.equals("series")) {
                return r1Values.stream().mapToDouble(Double::doubleValue).sum();
            } else {
                return 1.0 / r1Values.stream().mapToDouble(v -> 1.0/v).sum();
            }
        }

        /**
         * Вычисляет эффективное сопротивление нижнего плеча R2.
         *
         * <p>Для последовательного соединения: R = R1 + R2 + ... + Rn</p>
         * <p>Для параллельного соединения: 1/R = 1/R1 + 1/R2 + ... + 1/Rn</p>
         *
         * @return эффективное сопротивление R2 в омах
         */
        public double getR2() {
            if (r2Config.equals("series")) {
                return r2Values.stream().mapToDouble(Double::doubleValue).sum();
            } else {
                return 1.0 / r2Values.stream().mapToDouble(v -> 1.0/v).sum();
            }
        }

        /**
         * Возвращает строковое представление решения делителя.
         *
         * <p>Формат: "R1=[номиналы] (эффективное сопротивление Ω),
         * R2=[номиналы] (эффективное сопротивление Ω),
         * Vout=X.XXX V, Error=X.XX%, Power=X.XXX mW"</p>
         *
         * @return форматированная строка с параметрами решения
         */
        @Override
        public String toString() {
            return String.format("R1=%s (%.2f Ω), R2=%s (%.2f Ω), Vout=%.3f V, Error=%.2f%%, Power=%.3f mW",
                    r1Values, getR1(), r2Values, getR2(), vout, error, power * 1000);
        }
    }

    /**
     * Находит оптимальные решения делителя напряжения из стандартных номиналов резисторов.
     *
     * <p>Метод выполняет интеллектуальный поиск комбинаций резисторов для получения
     * требуемого выходного напряжения с заданной погрешностью. Рассматриваются варианты
     * с использованием 2, 3 и 4 резисторов в различных конфигурациях.</p>
     *
     * <p>Алгоритм работы:</p>
     * <ol>
     *   <li>Генерируется список доступных номиналов из указанного ряда (E6/E12/E24)</li>
     *   <li>Перебираются варианты с 2 резисторами (классический делитель)</li>
     *   <li>Перебираются варианты с 3 резисторами (4 конфигурации)</li>
     *   <li>Перебираются варианты с 4 резисторами (3 конфигурации)</li>
     *   <li>Все решения фильтруются по допустимой погрешности</li>
     *   <li>Результаты сортируются по погрешности, количеству элементов и мощности</li>
     *   <li>Возвращаются 10 лучших решений</li>
     * </ol>
     *
     * <p>Примеры конфигураций с 3 резисторами:</p>
     * <ul>
     *   <li>R1 | (R2 + R3) - один резистор сверху, два последовательно снизу</li>
     *   <li>R1 | (R2 || R3) - один резистор сверху, два параллельно снизу</li>
     *   <li>(R1 + R2) | R3 - два последовательно сверху, один снизу</li>
     *   <li>(R1 || R2) | R3 - два параллельно сверху, один снизу</li>
     * </ul>
     *
     * @param vin входное напряжение делителя в вольтах (должно быть &gt; 0)
     * @param vRequired требуемое выходное напряжение в вольтах (должно быть &gt; 0 и &lt; vin)
     * @param tolerance допустимая погрешность в процентах (например, 5.0 для ±5%)
     * @param series стандартный ряд резисторов: "E6", "E12" или "E24"
     * @param rMin минимальное сопротивление резисторов в омах (например, 1.0)
     * @param rMax максимальное сопротивление резисторов в омах (например, 1000000.0)
     * @return список до 10 лучших решений, отсортированных по погрешности
     *
     * @see DividerSolution
     */
    public static List<DividerSolution> findSolutions(
            double vin, double vRequired, double tolerance,
            String series, double rMin, double rMax) {

        List<DividerSolution> solutions = new ArrayList<>();
        List<Double> resistors = generateResistors(series, rMin, rMax);

        // 1. Два резистора (классический делитель)
        solutions.addAll(findTwoResistorSolutions(vin, vRequired, tolerance, resistors));

        // 2. Три резистора
        solutions.addAll(findThreeResistorSolutions(vin, vRequired, tolerance, resistors));

        // 3. Четыре резистора
        solutions.addAll(findFourResistorSolutions(vin, vRequired, tolerance, resistors));

        // Сортировка по погрешности, затем по количеству элементов, затем по мощности
        solutions.sort(Comparator
                .comparingDouble((DividerSolution s) -> Math.abs(s.error))
                .thenComparingInt(s -> s.r1Values.size() + s.r2Values.size())
                .thenComparingDouble(s -> s.power));

        return solutions.stream().limit(10).toList();
    }

    /**
     * Генерирует список доступных номиналов резисторов из стандартного ряда.
     *
     * <p>Берет базовые значения из указанного ряда (E6/E12/E24) и умножает их
     * на степени 10 от 10⁻² до 10⁷, чтобы получить все номиналы от 0.01 Ом до 10 МОм.
     * Фильтрует результаты по заданному диапазону [rMin, rMax].</p>
     *
     * @param series стандартный ряд: "E6", "E12" или "E24"
     * @param rMin минимальное сопротивление в омах
     * @param rMax максимальное сопротивление в омах
     * @return отсортированный список доступных номиналов в заданном диапазоне
     */
    private static List<Double> generateResistors(String series, double rMin, double rMax) {
        List<Double> resistors = new ArrayList<>();
        double[] baseValues = E_SERIES.get(series);

        for (int decade = -2; decade <= 7; decade++) {
            double multiplier = Math.pow(10, decade);
            for (double base : baseValues) {
                double value = base * multiplier;
                if (value >= rMin && value <= rMax) {
                    resistors.add(value);
                }
            }
        }
        return resistors;
    }

    /**
     * Находит решения с использованием двух резисторов (классический делитель).
     *
     * <p>Перебирает все возможные пары резисторов из доступного списка.
     * Для каждой пары вычисляет выходное напряжение, погрешность и мощность.
     * Сохраняет только решения, удовлетворяющие заданной погрешности.</p>
     *
     * <p>Схема: Vin ---[R1]--- Vout ---[R2]--- GND</p>
     *
     * @param vin входное напряжение в вольтах
     * @param vRequired требуемое выходное напряжение в вольтах
     * @param tolerance допустимая погрешность в процентах
     * @param resistors список доступных номиналов резисторов
     * @return список найденных решений с двумя резисторами
     */
    private static List<DividerSolution> findTwoResistorSolutions(
            double vin, double vRequired, double tolerance, List<Double> resistors) {

        List<DividerSolution> solutions = new ArrayList<>();

        for (double r1 : resistors) {
            for (double r2 : resistors) {
                DividerSolution sol = new DividerSolution();
                sol.r1Values = List.of(r1);
                sol.r2Values = List.of(r2);
                sol.r1Config = "series";
                sol.r2Config = "series";
                sol.vout = vin * r2 / (r1 + r2);
                sol.error = Math.abs((sol.vout - vRequired) / vRequired * 100);
                sol.power = (vin * vin) / (r1 + r2);

                if (sol.error <= tolerance) {
                    solutions.add(sol);
                }
            }
        }
        return solutions;
    }

    /**
     * Находит решения с использованием трех резисторов в различных конфигурациях.
     *
     * <p>Перебирает 4 возможные конфигурации с 3 резисторами:</p>
     * <ol>
     *   <li>R1 сверху, R2+R3 последовательно снизу</li>
     *   <li>R1 сверху, R2||R3 параллельно снизу</li>
     *   <li>R1+R2 последовательно сверху, R3 снизу</li>
     *   <li>R1||R2 параллельно сверху, R3 снизу</li>
     * </ol>
     *
     * <p>Для оптимизации производительности перебор ограничен первыми 50 резисторами
     * из списка (или меньше, если резисторов меньше 50).</p>
     *
     * @param vin входное напряжение в вольтах
     * @param vRequired требуемое выходное напряжение в вольтах
     * @param tolerance допустимая погрешность в процентах
     * @param resistors список доступных номиналов резисторов
     * @return список найденных решений с тремя резисторами
     */
    private static List<DividerSolution> findThreeResistorSolutions(
            double vin, double vRequired, double tolerance, List<Double> resistors) {

        List<DividerSolution> solutions = new ArrayList<>();
        int limit = Math.min(50, resistors.size());

        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                for (int k = 0; k < limit; k++) {
                    // R1 последовательно, R2+R3 последовательно
                    testConfiguration(vin, vRequired, tolerance, solutions,
                            List.of(resistors.get(i)), "series",
                            List.of(resistors.get(j), resistors.get(k)), "series");

                    // R1 последовательно, R2||R3 параллельно
                    testConfiguration(vin, vRequired, tolerance, solutions,
                            List.of(resistors.get(i)), "series",
                            List.of(resistors.get(j), resistors.get(k)), "parallel");

                    // R1+R2 последовательно, R3
                    testConfiguration(vin, vRequired, tolerance, solutions,
                            List.of(resistors.get(i), resistors.get(j)), "series",
                            List.of(resistors.get(k)), "series");

                    // R1||R2 параллельно, R3
                    testConfiguration(vin, vRequired, tolerance, solutions,
                            List.of(resistors.get(i), resistors.get(j)), "parallel",
                            List.of(resistors.get(k)), "series");
                }
            }
        }
        return solutions;
    }

    /**
     * Находит решения с использованием четырех резисторов в различных конфигурациях.
     *
     * <p>Перебирает 3 возможные конфигурации с 4 резисторами:</p>
     * <ol>
     *   <li>R1+R2 последовательно сверху, R3+R4 последовательно снизу</li>
     *   <li>R1||R2 параллельно сверху, R3||R4 параллельно снизу</li>
     *   <li>R1+R2 последовательно сверху, R3||R4 параллельно снизу</li>
     * </ol>
     *
     * <p>Для оптимизации производительности перебор ограничен первыми 30 резисторами
     * из списка (или меньше, если резисторов меньше 30).</p>
     *
     * @param vin входное напряжение в вольтах
     * @param vRequired требуемое выходное напряжение в вольтах
     * @param tolerance допустимая погрешность в процентах
     * @param resistors список доступных номиналов резисторов
     * @return список найденных решений с четырьмя резисторами
     */
    private static List<DividerSolution> findFourResistorSolutions(
            double vin, double vRequired, double tolerance, List<Double> resistors) {

        List<DividerSolution> solutions = new ArrayList<>();
        int limit = Math.min(30, resistors.size());

        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                for (int k = 0; k < limit; k++) {
                    for (int l = 0; l < limit; l++) {
                        // Различные конфигурации
                        testConfiguration(vin, vRequired, tolerance, solutions,
                                List.of(resistors.get(i), resistors.get(j)), "series",
                                List.of(resistors.get(k), resistors.get(l)), "series");

                        testConfiguration(vin, vRequired, tolerance, solutions,
                                List.of(resistors.get(i), resistors.get(j)), "parallel",
                                List.of(resistors.get(k), resistors.get(l)), "parallel");

                        testConfiguration(vin, vRequired, tolerance, solutions,
                                List.of(resistors.get(i), resistors.get(j)), "series",
                                List.of(resistors.get(k), resistors.get(l)), "parallel");
                    }
                }
            }
        }
        return solutions;
    }

    /**
     * Тестирует конкретную конфигурацию делителя и добавляет её в список решений,
     * если погрешность находится в допустимых пределах.
     *
     * <p>Метод создает объект DividerSolution с указанными номиналами и конфигурациями,
     * вычисляет эффективные сопротивления R1 и R2, выходное напряжение, погрешность
     * и потребляемую мощность. Если погрешность не превышает заданную, решение
     * добавляется в список.</p>
     *
     * @param vin входное напряжение в вольтах
     * @param vRequired требуемое выходное напряжение в вольтах
     * @param tolerance допустимая погрешность в процентах
     * @param solutions список для добавления найденных решений
     * @param r1Values список номиналов для верхнего плеча
     * @param r1Config конфигурация верхнего плеча ("series" или "parallel")
     * @param r2Values список номиналов для нижнего плеча
     * @param r2Config конфигурация нижнего плеча ("series" или "parallel")
     */
    private static void testConfiguration(
            double vin, double vRequired, double tolerance,
            List<DividerSolution> solutions,
            List<Double> r1Values, String r1Config,
            List<Double> r2Values, String r2Config) {

        DividerSolution sol = new DividerSolution();
        sol.r1Values = new ArrayList<>(r1Values);
        sol.r2Values = new ArrayList<>(r2Values);
        sol.r1Config = r1Config;
        sol.r2Config = r2Config;

        double r1 = sol.getR1();
        double r2 = sol.getR2();
        sol.vout = vin * r2 / (r1 + r2);
        sol.error = Math.abs((sol.vout - vRequired) / vRequired * 100);
        sol.power = (vin * vin) / (r1 + r2);

        if (sol.error <= tolerance) {
            solutions.add(sol);
        }
    }
}
