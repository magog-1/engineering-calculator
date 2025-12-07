package com.engineeringcalc.ui;

import com.engineeringcalc.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Окно главного меню приложения Engineering Calculator.
 *
 * <p>Предоставляет пользователю доступ к основным функциям приложения:
 * калькуляторам, истории вычислений и выходу из системы.
 * Отображает приветствие с именем пользователя и его ролью.</p>
 *
 * <p>Доступные функции в меню:</p>
 * <ul>
 *   <li>Калькулятор закона Ома</li>
 *   <li>Калькулятор делителя напряжения</li>
 *   <li>Просмотр истории вычислений</li>
 *   <li>Выход из аккаунта</li>
 * </ul>
 *
 * @author magog-1
 * @version 1.0
 * @since 2025-12-08
 * @see User
 * @see OhmsLawWindow
 * @see VoltageDividerWindow
 * @see HistoryWindow
 */
public class MainMenuWindow {
    /** Главное окно приложения JavaFX */
    private Stage stage;

    /** Текущий аутентифицированный пользователь */
    private User currentUser;

    /**
     * Создает новое окно главного меню.
     *
     * @param stage главное окно приложения JavaFX
     * @param currentUser текущий аутентифицированный пользователь
     */
    public MainMenuWindow(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
    }

    /**
     * Отображает главное меню с кнопками навигации.
     *
     * <p>Создает интерфейс с приветствием, информацией о роли пользователя
     * и кнопками для доступа к различным функциям калькулятора.
     * Настраивает обработчики событий для всех кнопок навигации.</p>
     *
     * @see OhmsLawWindow#show()
     * @see VoltageDividerWindow#show()
     * @see HistoryWindow#show()
     * @see LoginWindow#show()
     */
    public void show() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Добро пожаловать, " + currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label roleLabel = new Label("Роль: " + currentUser.getRole());
        roleLabel.setStyle("-fx-font-size: 14px;");

        Button ohmsLawBtn = new Button("Калькулятор закона Ома");
        ohmsLawBtn.setPrefWidth(300);
        ohmsLawBtn.setStyle("-fx-font-size: 14px; -fx-padding: 15px;");

        Button voltageDividerBtn = new Button("Калькулятор делителя напряжения");
        voltageDividerBtn.setPrefWidth(300);
        voltageDividerBtn.setStyle("-fx-font-size: 14px; -fx-padding: 15px;");

        Button historyBtn = new Button("История вычислений");
        historyBtn.setPrefWidth(300);
        historyBtn.setStyle("-fx-font-size: 14px; -fx-padding: 15px;");

        Button logoutBtn = new Button("Выход");
        logoutBtn.setPrefWidth(300);
        logoutBtn.setStyle("-fx-font-size: 14px; -fx-padding: 15px; -fx-background-color: #e74c3c; -fx-text-fill: white;");

        ohmsLawBtn.setOnAction(e ->
                new OhmsLawWindow(stage, currentUser, this).show()
        );

        voltageDividerBtn.setOnAction(e ->
                new VoltageDividerWindow(stage, currentUser, this).show()
        );

        historyBtn.setOnAction(e ->
                new HistoryWindow(stage, currentUser, this).show()
        );

        logoutBtn.setOnAction(e -> {
            new LoginWindow(stage).show();
        });

        root.getChildren().addAll(
                welcomeLabel, roleLabel,
                ohmsLawBtn, voltageDividerBtn, historyBtn, logoutBtn
        );

        Scene scene = new Scene(root, 500, 450);
        stage.setScene(scene);
        stage.setTitle("Главное меню");
    }
}
