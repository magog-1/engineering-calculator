package com.engineeringcalc.calculator;

import java.util.*;

public class VoltageDividerCalculator {

    // Стандартные ряды резисторов
    private static final Map<String, double[]> E_SERIES = Map.of(
            "E6", new double[]{1.0, 1.5, 2.2, 3.3, 4.7, 6.8},
            "E12", new double[]{1.0, 1.2, 1.5, 1.8, 2.2, 2.7, 3.3, 3.9, 4.7, 5.6, 6.8, 8.2},
            "E24", new double[]{1.0, 1.1, 1.2, 1.3, 1.5, 1.6, 1.8, 2.0, 2.2, 2.4, 2.7, 3.0,
                    3.3, 3.6, 3.9, 4.3, 4.7, 5.1, 5.6, 6.2, 6.8, 7.5, 8.2, 9.1}
    );

    public static class DividerSolution {
        public List<Double> r1Values; // Верхнее плечо
        public List<Double> r2Values; // Нижнее плечо
        public String r1Config; // "series" или "parallel"
        public String r2Config;
        public double vout;
        public double error; // Погрешность в процентах
        public double power; // Потребляемая мощность

        public double getR1() {
            if (r1Config.equals("series")) {
                return r1Values.stream().mapToDouble(Double::doubleValue).sum();
            } else {
                return 1.0 / r1Values.stream().mapToDouble(v -> 1.0/v).sum();
            }
        }

        public double getR2() {
            if (r2Config.equals("series")) {
                return r2Values.stream().mapToDouble(Double::doubleValue).sum();
            } else {
                return 1.0 / r2Values.stream().mapToDouble(v -> 1.0/v).sum();
            }
        }

        @Override
        public String toString() {
            return String.format("R1=%s (%.2f Ω), R2=%s (%.2f Ω), Vout=%.3f V, Error=%.2f%%, Power=%.3f mW",
                    r1Values, getR1(), r2Values, getR2(), vout, error, power * 1000);
        }
    }

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