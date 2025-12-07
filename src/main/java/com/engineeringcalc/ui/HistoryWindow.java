package com.engineeringcalc.ui;

import com.engineeringcalc.model.User;
import com.engineeringcalc.database.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class HistoryWindow {
    private Stage stage;
    private User currentUser;
    private MainMenuWindow mainMenu;

    public static class HistoryRecord {
        private final SimpleStringProperty id;
        private final SimpleStringProperty username;
        private final SimpleStringProperty type;
        private final SimpleStringProperty input;
        private final SimpleStringProperty result;
        private final SimpleStringProperty date;

        public HistoryRecord(String id, String username, String type,
                             String input, String result, String date) {
            this.id = new SimpleStringProperty(id);
            this.username = new SimpleStringProperty(username);
            this.type = new SimpleStringProperty(type);
            this.input = new SimpleStringProperty(input);
            this.result = new SimpleStringProperty(result);
            this.date = new SimpleStringProperty(date);
        }

        public String getId() { return id.get(); }
        public String getUsername() { return username.get(); }
        public String getType() { return type.get(); }
        public String getInput() { return input.get(); }
        public String getResult() { return result.get(); }
        public String getDate() { return date.get(); }
    }

    public HistoryWindow(Stage stage, User currentUser, MainMenuWindow mainMenu) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.mainMenu = mainMenu;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Заголовок
        Label titleLabel = new Label("История вычислений");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Фильтры
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Тип:");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Все", "Закон Ома", "Делитель напряжения");
        typeFilter.setValue("Все");

        CheckBox showAllUsers = new CheckBox("Показать всех пользователей");
        showAllUsers.setDisable(!currentUser.isAdmin());

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setStyle("-fx-font-size: 12px;");

        filterBox.getChildren().addAll(filterLabel, typeFilter, showAllUsers, refreshBtn);

        VBox topBox = new VBox(10, titleLabel, filterBox);
        topBox.setAlignment(Pos.TOP_CENTER);

        // Таблица
        TableView<HistoryRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<HistoryRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().id);
        idCol.setPrefWidth(50);

        TableColumn<HistoryRecord, String> userCol = new TableColumn<>("Пользователь");
        userCol.setCellValueFactory(data -> data.getValue().username);
        userCol.setPrefWidth(120);

        TableColumn<HistoryRecord, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(data -> data.getValue().type);
        typeCol.setPrefWidth(150);

        TableColumn<HistoryRecord, String> inputCol = new TableColumn<>("Входные данные");
        inputCol.setCellValueFactory(data -> data.getValue().input);
        inputCol.setPrefWidth(200);

        TableColumn<HistoryRecord, String> resultCol = new TableColumn<>("Результат");
        resultCol.setCellValueFactory(data -> data.getValue().result);
        resultCol.setPrefWidth(300);

        TableColumn<HistoryRecord, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(data -> data.getValue().date);
        dateCol.setPrefWidth(150);

        if (currentUser.isAdmin()) {
            table.getColumns().addAll(idCol, userCol, typeCol, inputCol, resultCol, dateCol);
        } else {
            table.getColumns().addAll(idCol, typeCol, inputCol, resultCol, dateCol);
        }

        // Детали
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(100);
        detailsArea.setWrapText(true);
        detailsArea.setPromptText("Выберите запись для просмотра деталей");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                detailsArea.setText(String.format(
                        "ID: %s\nПользователь: %s\nТип: %s\nДата: %s\n\nВходные данные:\n%s\n\nРезультат:\n%s",
                        newVal.getId(), newVal.getUsername(), newVal.getType(),
                        newVal.getDate(), newVal.getInput(), newVal.getResult()
                ));
            }
        });

        VBox centerBox = new VBox(10, table, new Label("Детали:"), detailsArea);
        centerBox.setPadding(new Insets(10));

        // Кнопки
        Button deleteBtn = new Button("Удалить выбранное");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        Button backBtn = new Button("Назад");
        backBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");

        HBox buttonBox = new HBox(10, deleteBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // Загрузка данных
        Runnable loadData = () -> {
            ObservableList<HistoryRecord> data = FXCollections.observableArrayList();

            try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                String query;

                if (currentUser.isAdmin() && showAllUsers.isSelected()) {
                    query = """
                        SELECT h.id, u.username, h.calculation_type, h.input_data, 
                               h.result_data, h.created_at
                        FROM calculation_history h
                        JOIN users u ON h.user_id = u.id
                        ORDER BY h.created_at DESC
                    """;
                } else {
                    query = """
                        SELECT h.id, u.username, h.calculation_type, h.input_data, 
                               h.result_data, h.created_at
                        FROM calculation_history h
                        JOIN users u ON h.user_id = u.id
                        WHERE h.user_id = ?
                        ORDER BY h.created_at DESC
                    """;
                }

                PreparedStatement stmt = conn.prepareStatement(query);
                if (!currentUser.isAdmin() || !showAllUsers.isSelected()) {
                    stmt.setInt(1, currentUser.getId());
                }

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String type = rs.getString("calculation_type");
                    String typeFilterValue = typeFilter.getValue();

                    if (typeFilterValue.equals("Все") || type.equals(typeFilterValue)) {
                        data.add(new HistoryRecord(
                                rs.getString("id"),
                                rs.getString("username"),
                                type,
                                rs.getString("input_data"),
                                rs.getString("result_data"),
                                rs.getString("created_at")
                        ));
                    }
                }

                table.setItems(data);

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Ошибка загрузки данных");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        };

        loadData.run();

        // Обработчики
        refreshBtn.setOnAction(e -> loadData.run());
        typeFilter.setOnAction(e -> loadData.run());
        showAllUsers.setOnAction(e -> loadData.run());

        deleteBtn.setOnAction(e -> {
            HistoryRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("Ничего не выбрано");
                alert.setContentText("Выберите запись для удаления");
                alert.showAndWait();
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText("Удалить запись?");
            confirm.setContentText("Вы уверены, что хотите удалить эту запись?");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                try (Connection conn = DatabaseManager.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM calculation_history WHERE id = ?")) {

                    stmt.setString(1, selected.getId());
                    stmt.executeUpdate();
                    loadData.run();
                    detailsArea.clear();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        backBtn.setOnAction(e -> mainMenu.show());

        root.setTop(topBox);
        root.setCenter(centerBox);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        stage.setTitle("История");
    }
}