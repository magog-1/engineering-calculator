package com.engineeringcalc.ui;

import com.engineeringcalc.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrationWindow {
    private Stage stage;
    private LoginWindow loginWindow;

    public RegistrationWindow(Stage stage, LoginWindow loginWindow) {
        this.stage = stage;
        this.loginWindow = loginWindow;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Регистрация");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Логин:");
        TextField userField = new TextField();
        userField.setPromptText("Введите логин");

        Label passLabel = new Label("Пароль:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Минимум 6 символов");

        Label passConfirmLabel = new Label("Подтверждение:");
        PasswordField passConfirmField = new PasswordField();
        passConfirmField.setPromptText("Повторите пароль");

        Label roleLabel = new Label("Роль:");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("USER", "ADMIN");
        roleBox.setValue("USER");

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(passConfirmLabel, 0, 2);
        grid.add(passConfirmField, 1, 2);
        grid.add(roleLabel, 0, 3);
        grid.add(roleBox, 1, 3);

        Button registerBtn = new Button("Зарегистрироваться");
        registerBtn.setDefaultButton(true);
        registerBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");

        Button backBtn = new Button("Назад");
        backBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");

        Label statusLabel = new Label();

        registerBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText();
            String confirmPassword = passConfirmField.getText();
            String role = roleBox.getValue();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Заполните все поля");
                return;
            }

            if (password.length() < 6) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Пароль должен быть минимум 6 символов");
                return;
            }

            if (!password.equals(confirmPassword)) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Пароли не совпадают");
                return;
            }

            if (User.register(username, password, role)) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Регистрация успешна! Войдите в систему.");
                userField.clear();
                passField.clear();
                passConfirmField.clear();
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Ошибка: пользователь уже существует");
            }
        });

        backBtn.setOnAction(e -> loginWindow.show());

        root.getChildren().addAll(
                titleLabel, grid, registerBtn, backBtn, statusLabel
        );

        Scene scene = new Scene(root, 450, 400);
        stage.setScene(scene);
        stage.setTitle("Регистрация");
    }
}