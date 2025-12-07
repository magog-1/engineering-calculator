package com.engineeringcalc.ui;

import com.engineeringcalc.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginWindow {
    private Stage stage;
    private User currentUser;

    public LoginWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Инженерный Калькулятор");
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
        passField.setPromptText("Введите пароль");

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);

        Button loginBtn = new Button("Войти");
        loginBtn.setDefaultButton(true);
        loginBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");

        Button registerBtn = new Button("Регистрация");
        registerBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Заполните все поля");
                return;
            }

            User user = User.authenticate(username, password);
            if (user != null) {
                currentUser = user;
                openMainMenu();
            } else {
                statusLabel.setText("Неверный логин или пароль");
            }
        });

        registerBtn.setOnAction(e -> {
            new RegistrationWindow(stage, this).show();
        });

        root.getChildren().addAll(
                titleLabel, grid, loginBtn, registerBtn, statusLabel
        );

        Scene scene = new Scene(root, 400, 350);
        stage.setScene(scene);
        stage.setTitle("Вход");
        stage.show();
    }

    private void openMainMenu() {
        new MainMenuWindow(stage, currentUser).show();
    }

    public User getCurrentUser() {
        return currentUser;
    }
}