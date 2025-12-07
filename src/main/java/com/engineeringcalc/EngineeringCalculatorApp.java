package com.engineeringcalc;

import com.engineeringcalc.database.DatabaseManager;
import com.engineeringcalc.ui.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class EngineeringCalculatorApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Инициализация базы данных
        DatabaseManager.getInstance();

        // Показываем окно входа
        LoginWindow loginWindow = new LoginWindow(primaryStage);
        loginWindow.show();

        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}