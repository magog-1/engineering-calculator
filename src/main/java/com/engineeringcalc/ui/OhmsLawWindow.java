package com.engineeringcalc.ui;

import com.engineeringcalc.calculator.OhmsLawCalculator;
import com.engineeringcalc.model.User;
import com.engineeringcalc.database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class OhmsLawWindow {
    private Stage stage;
    private User currentUser;
    private MainMenuWindow mainMenu;

    public OhmsLawWindow(Stage stage, User currentUser, MainMenuWindow mainMenu) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.mainMenu = mainMenu;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Заголовок
        Label titleLabel = new Label("Калькулятор закона Ома");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 20, 0));

        // Форма ввода
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Напряжение
        Label vLabel = new Label("Напряжение (V):");
        TextField vField = new TextField();
        vField.setPromptText("Оставьте пустым для расчета");
        ComboBox<String> vUnit = new ComboBox<>();
        vUnit.getItems().addAll("В", "мВ");
        vUnit.setValue("В");

        // Ток
        Label iLabel = new Label("Ток (I):");
        TextField iField = new TextField();
        iField.setPromptText("Оставьте пустым для расчета");
        ComboBox<String> iUnit = new ComboBox<>();
        iUnit.getItems().addAll("А", "мА");
        iUnit.setValue("А");

        // Сопротивление
        Label rLabel = new Label("Сопротивление (R):");
        TextField rField = new TextField();
        rField.setPromptText("Оставьте пустым для расчета");
        ComboBox<String> rUnit = new ComboBox<>();
        rUnit.getItems().addAll("Ом", "кОм", "МОм");
        rUnit.setValue("Ом");

        grid.add(vLabel, 0, 0);
        grid.add(vField, 1, 0);
        grid.add(vUnit, 2, 0);

        grid.add(iLabel, 0, 1);
        grid.add(iField, 1, 1);
        grid.add(iUnit, 2, 1);

        grid.add(rLabel, 0, 2);
        grid.add(rField, 1, 2);
        grid.add(rUnit, 2, 2);

        // Кнопки
        Button calculateBtn = new Button("Рассчитать");
        calculateBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");
        calculateBtn.setDefaultButton(true);

        Button clearBtn = new Button("Очистить");
        clearBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");

        Button backBtn = new Button("Назад");
        backBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");

        HBox buttonBox = new HBox(10, calculateBtn, clearBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        // Область результата
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(150);
        resultArea.setStyle("-fx-font-size: 13px; -fx-font-family: monospace;");

        // Обработчики
        calculateBtn.setOnAction(e -> {
            try {
                Double v = vField.getText().trim().isEmpty() ? null :
                        OhmsLawCalculator.convertVoltage(
                                Double.parseDouble(vField.getText()), vUnit.getValue());

                Double i = iField.getText().trim().isEmpty() ? null :
                        OhmsLawCalculator.convertCurrent(
                                Double.parseDouble(iField.getText()), iUnit.getValue());

                Double r = rField.getText().trim().isEmpty() ? null :
                        OhmsLawCalculator.convertResistance(
                                Double.parseDouble(rField.getText()), rUnit.getValue());

                OhmsLawCalculator.Result result = OhmsLawCalculator.calculate(v, i, r);

                String output = String.format("""
                    ═══════════════════════════════════════
                    РЕЗУЛЬТАТЫ РАСЧЕТА ПО ЗАКОНУ ОМА
                    ═══════════════════════════════════════
                    
                    Напряжение (V):      %.6f В  (%.3f мВ)
                    Ток (I):             %.6f А  (%.3f мА)
                    Сопротивление (R):   %.3f Ом (%.3f кОм)
                    
                    Мощность (P = V×I):  %.6f Вт
                    
                    Вычислен параметр: %s
                    ═══════════════════════════════════════
                    """,
                        result.voltage, result.voltage * 1000,
                        result.current, result.current * 1000,
                        result.resistance, result.resistance / 1000,
                        result.voltage * result.current,
                        result.calculated
                );

                resultArea.setText(output);

                // Сохранить в историю
                saveToHistory(v, i, r, result);

            } catch (NumberFormatException ex) {
                resultArea.setText("ОШИБКА: Введите корректные числовые значения");
            } catch (IllegalArgumentException ex) {
                resultArea.setText("ОШИБКА: " + ex.getMessage());
            }
        });

        clearBtn.setOnAction(e -> {
            vField.clear();
            iField.clear();
            rField.clear();
            resultArea.clear();
        });

        backBtn.setOnAction(e -> mainMenu.show());

        VBox centerBox = new VBox(20, grid, buttonBox, resultArea);
        centerBox.setAlignment(Pos.TOP_CENTER);

        root.setTop(titleBox);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 600, 550);
        stage.setScene(scene);
        stage.setTitle("Закон Ома");
    }

    private void saveToHistory(Double v, Double i, Double r, OhmsLawCalculator.Result result) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO calculation_history (user_id, calculation_type, input_data, result_data) VALUES (?, ?, ?, ?)")) {

            String input = String.format("V=%s, I=%s, R=%s",
                    v == null ? "?" : v, i == null ? "?" : i, r == null ? "?" : r);

            stmt.setInt(1, currentUser.getId());
            stmt.setString(2, "Закон Ома");
            stmt.setString(3, input);
            stmt.setString(4, result.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}