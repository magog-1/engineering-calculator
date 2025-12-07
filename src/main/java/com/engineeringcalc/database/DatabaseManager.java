package com.engineeringcalc.database;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:engineering_calc.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Таблица пользователей
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('USER', 'ADMIN')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Таблица истории вычислений
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS calculation_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    calculation_type TEXT NOT NULL,
                    input_data TEXT NOT NULL,
                    result_data TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Создаем админа по умолчанию (пароль: admin)
            String adminHash = BCrypt.hashpw("admin", BCrypt.gensalt());
            stmt.execute(
                    "INSERT OR IGNORE INTO users (username, password_hash, role) " +
                            "VALUES ('admin', '" + adminHash + "', 'ADMIN')"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}