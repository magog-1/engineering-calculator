package com.engineeringcalc.ui;

import com.engineeringcalc.calculator.VoltageDividerCalculator;
import com.engineeringcalc.calculator.VoltageDividerCalculator.DividerSolution;
import com.engineeringcalc.model.User;
import com.engineeringcalc.database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.*;
import java.util.List;

public class VoltageDividerWindow {
    private Stage stage;
    private User currentUser;
    private MainMenuWindow mainMenu;
    private Canvas canvas;
    private List<DividerSolution> solutions;
    private int currentSolutionIndex = 0;

    public VoltageDividerWindow(Stage stage, User currentUser, MainMenuWindow mainMenu) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.mainMenu = mainMenu;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Заголовок
        Label titleLabel = new Label("Калькулятор делителя напряжения");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Форма ввода
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(15));

        TextField vinField = new TextField("12");
        TextField voutField = new TextField("5");
        TextField toleranceField = new TextField("1");

        ComboBox<String> seriesBox = new ComboBox<>();
        seriesBox.getItems().addAll("E6", "E12", "E24");
        seriesBox.setValue("E12");

        TextField rMinField = new TextField("100");
        TextField rMaxField = new TextField("1000000");

        inputGrid.add(new Label("Vin (В):"), 0, 0);
        inputGrid.add(vinField, 1, 0);
        inputGrid.add(new Label("Vout треб. (В):"), 2, 0);
        inputGrid.add(voutField, 3, 0);

        inputGrid.add(new Label("Допуск (%):"), 0, 1);
        inputGrid.add(toleranceField, 1, 1);
        inputGrid.add(new Label("Ряд:"), 2, 1);
        inputGrid.add(seriesBox, 3, 1);

        inputGrid.add(new Label("R мин (Ом):"), 0, 2);
        inputGrid.add(rMinField, 1, 2);
        inputGrid.add(new Label("R макс (Ом):"), 2, 2);
        inputGrid.add(rMaxField, 3, 2);

        Button calculateBtn = new Button("Найти решения");
        calculateBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 20px;");

        Button backBtn = new Button("Назад");
        backBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 20px;");

        HBox buttonBox = new HBox(10, calculateBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(10, titleLabel, inputGrid, buttonBox);
        topBox.setAlignment(Pos.TOP_CENTER);

        // Область результатов и схемы
        ListView<String> solutionsList = new ListView<>();
        solutionsList.setPrefHeight(150);

        canvas = new Canvas(400, 400);

        Label schemeLabel = new Label("Схема делителя");
        schemeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button prevBtn = new Button("◀ Предыдущая");
        Button nextBtn = new Button("Следующая ▶");
        Button saveBtn = new Button("Сохранить в историю");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        HBox navBox = new HBox(10, prevBtn, nextBtn, saveBtn);
        navBox.setAlignment(Pos.CENTER);

        VBox canvasBox = new VBox(10, schemeLabel, canvas, navBox);
        canvasBox.setAlignment(Pos.CENTER);

        HBox centerBox = new HBox(20, solutionsList, canvasBox);
        centerBox.setPadding(new Insets(15));

        // Обработчики
        calculateBtn.setOnAction(e -> {
            try {
                double vin = Double.parseDouble(vinField.getText());
                double vout = Double.parseDouble(voutField.getText());
                double tolerance = Double.parseDouble(toleranceField.getText());
                String series = seriesBox.getValue();
                double rMin = Double.parseDouble(rMinField.getText());
                double rMax = Double.parseDouble(rMaxField.getText());

                solutions = VoltageDividerCalculator.findSolutions(
                        vin, vout, tolerance, series, rMin, rMax);

                currentSolutionIndex = 0;
                solutionsList.getItems().clear();

                if (solutions.isEmpty()) {
                    solutionsList.getItems().add("Решения не найдены. Попробуйте изменить параметры.");
                } else {
                    for (int i = 0; i < solutions.size(); i++) {
                        DividerSolution sol = solutions.get(i);
                        solutionsList.getItems().add(String.format(
                                "%d. Vout=%.3fВ, Error=%.2f%%, P=%.2fмВт, Элементов=%d",
                                i + 1, sol.vout, sol.error, sol.power * 1000,
                                sol.r1Values.size() + sol.r2Values.size()
                        ));
                    }
                    drawScheme(solutions.get(0), vin);
                }

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Неверный формат данных");
                alert.setContentText("Проверьте правильность введенных значений");
                alert.showAndWait();
            }
        });

        solutionsList.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && solutions != null && newVal.intValue() < solutions.size()) {
                currentSolutionIndex = newVal.intValue();
                double vin = Double.parseDouble(vinField.getText());
                drawScheme(solutions.get(currentSolutionIndex), vin);
            }
        });

        prevBtn.setOnAction(e -> {
            if (solutions != null && !solutions.isEmpty() && currentSolutionIndex > 0) {
                currentSolutionIndex--;
                solutionsList.getSelectionModel().select(currentSolutionIndex);
            }
        });

        nextBtn.setOnAction(e -> {
            if (solutions != null && currentSolutionIndex < solutions.size() - 1) {
                currentSolutionIndex++;
                solutionsList.getSelectionModel().select(currentSolutionIndex);
            }
        });

        saveBtn.setOnAction(e -> {
            if (solutions != null && !solutions.isEmpty()) {
                DividerSolution sol = solutions.get(currentSolutionIndex);
                saveToHistory(vinField.getText(), voutField.getText(), sol);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Сохранено");
                alert.setHeaderText(null);
                alert.setContentText("Решение сохранено в историю");
                alert.showAndWait();
            }
        });

        backBtn.setOnAction(e -> mainMenu.show());

        root.setTop(topBox);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 950, 700);
        stage.setScene(scene);
        stage.setTitle("Делитель напряжения");
    }

    private void drawScheme(DividerSolution sol, double vin) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double centerX = canvas.getWidth() / 2;
        double startY = 50;
        double resistorHeight = 60;
        double resistorWidth = 15;
        double spacing = 20;

        // VIN
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 14));
        gc.fillText(String.format("VIN = %.2f В", vin), centerX - 30, startY - 10);

        // Линия от VIN
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(centerX, startY, centerX, startY + spacing);

        double currentY = startY + spacing;

        // Верхнее плечо (R1)
        drawResistorArm(gc, centerX, currentY, sol.r1Values, sol.r1Config, "R1");

        if (sol.r1Config.equals("series")) {
            currentY += sol.r1Values.size() * (resistorHeight + spacing);
        } else {
            currentY += resistorHeight + spacing;
        }

        // VOUT (средняя точка)
        gc.setFill(Color.RED);
        gc.fillOval(centerX - 5, currentY - 5, 10, 10);
        gc.setFill(Color.RED);
        gc.fillText(String.format("VOUT = %.3f В", sol.vout), centerX + 15, currentY + 5);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(centerX, currentY, centerX, currentY + spacing);
        currentY += spacing;

        // Нижнее плечо (R2)
        drawResistorArm(gc, centerX, currentY, sol.r2Values, sol.r2Config, "R2");

        if (sol.r2Config.equals("series")) {
            currentY += sol.r2Values.size() * (resistorHeight + spacing);
        } else {
            currentY += resistorHeight + spacing;
        }

        // GND
        gc.strokeLine(centerX, currentY, centerX, currentY + spacing);
        currentY += spacing;

        gc.setFill(Color.BLACK);
        gc.fillText("GND", centerX - 15, currentY + 15);

        // Символ земли
        for (int i = 0; i < 3; i++) {
            int width = 30 - i * 8;
            gc.strokeLine(centerX - width/2, currentY + i * 5,
                    centerX + width/2, currentY + i * 5);
        }
    }

    private void drawResistorArm(GraphicsContext gc, double centerX, double startY,
                                 List<Double> values, String config, String label) {
        double resistorHeight = 60;
        double resistorWidth = 15;
        double spacing = 20;

        if (config.equals("series")) {
            // Последовательное соединение
            double y = startY;
            for (int i = 0; i < values.size(); i++) {
                drawResistor(gc, centerX, y, resistorWidth, resistorHeight,
                        formatResistance(values.get(i)));
                y += resistorHeight + spacing;
            }
        } else {
            // Параллельное соединение
            double totalWidth = values.size() * 50;
            double startX = centerX - totalWidth / 2 + 25;

            // Верхняя горизонтальная линия
            gc.strokeLine(centerX, startY, centerX, startY + spacing);
            gc.strokeLine(centerX - totalWidth/2, startY + spacing,
                    centerX + totalWidth/2, startY + spacing);

            // Резисторы параллельно
            for (int i = 0; i < values.size(); i++) {
                double x = startX + i * 50;
                gc.strokeLine(x, startY + spacing, x, startY + spacing + 10);
                drawResistor(gc, x, startY + spacing + 10, resistorWidth, resistorHeight,
                        formatResistance(values.get(i)));
                gc.strokeLine(x, startY + spacing + 10 + resistorHeight,
                        x, startY + spacing + 10 + resistorHeight + 10);
            }

            // Нижняя горизонтальная линия
            gc.strokeLine(centerX - totalWidth/2, startY + spacing + resistorHeight + 20,
                    centerX + totalWidth/2, startY + spacing + resistorHeight + 20);
            gc.strokeLine(centerX, startY + spacing + resistorHeight + 20,
                    centerX, startY + spacing + resistorHeight + 20 + spacing);
        }
    }

    private void drawResistor(GraphicsContext gc, double x, double y,
                              double width, double height, String value) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        // Зигзаг резистора
        double segmentHeight = height / 8;
        gc.strokeLine(x, y, x, y + segmentHeight);

        for (int i = 0; i < 6; i++) {
            double y1 = y + segmentHeight + i * segmentHeight;
            double y2 = y1 + segmentHeight;
            double offset = (i % 2 == 0) ? width : -width;
            gc.strokeLine(x, y1, x + offset, y1 + segmentHeight/2);
            gc.strokeLine(x + offset, y1 + segmentHeight/2, x, y2);
        }

        gc.strokeLine(x, y + height - segmentHeight, x, y + height);

        // Значение сопротивления
        gc.setFill(Color.BLUE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 11));
        gc.fillText(value, x + width + 5, y + height/2);
    }

    private String formatResistance(double r) {
        if (r >= 1_000_000) {
            return String.format("%.2f МОм", r / 1_000_000);
        } else if (r >= 1000) {
            return String.format("%.2f кОм", r / 1000);
        } else {
            return String.format("%.1f Ом", r);
        }
    }

    private void saveToHistory(String vin, String vout, DividerSolution sol) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO calculation_history (user_id, calculation_type, input_data, result_data) VALUES (?, ?, ?, ?)")) {

            String input = String.format("Vin=%s В, Vout_req=%s В", vin, vout);

            stmt.setInt(1, currentUser.getId());
            stmt.setString(2, "Делитель напряжения");
            stmt.setString(3, input);
            stmt.setString(4, sol.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}