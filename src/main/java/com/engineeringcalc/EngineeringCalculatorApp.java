package com.engineeringcalc;

import com.engineeringcalc.database.DatabaseManager;
import com.engineeringcalc.ui.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Главный класс приложения Engineering Calculator.
 *
 * <p>Это JavaFX приложение для инженерных расчетов по закону Ома
 * и делителю напряжения. Приложение поддерживает систему пользователей
 * с аутентификацией и сохранением истории вычислений.</p>
 *
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Калькулятор по закону Ома (V = I * R)</li>
 *   <li>Калькулятор делителя напряжения</li>
 *   <li>Система пользователей с ролями USER и ADMIN</li>
 *   <li>История вычислений для каждого пользователя</li>
 *   <li>Безопасное хранение паролей с использованием BCrypt</li>
 * </ul>
 *
 * <p>При запуске приложение инициализирует базу данных SQLite
 * и отображает окно входа в систему.</p>
 *
 * @author magog-1
 * @version 1.0
 * @since 2025-12-08
 * @see DatabaseManager
 * @see LoginWindow
 * @see com.engineeringcalc.calculator.OhmsLawCalculator
 * @see com.engineeringcalc.calculator.VoltageDividerCalculator
 */
public class EngineeringCalculatorApp extends Application {

    /**
     * Точка входа в JavaFX приложение.
     *
     * <p>Инициализирует базу данных и отображает окно входа.
     * Настраивает обработчик закрытия приложения.</p>
     *
     * @param primaryStage главное окно приложения JavaFX
     * @see Application#start(Stage)
     */
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

    /**
     * Главная точка входа в приложение.
     *
     * <p>Запускает JavaFX приложение. Этот метод вызывается
     * виртуальной машиной Java при запуске программы.</p>
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
