package com.engineeringcalc.database;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * Менеджер базы данных для приложения Engineering Calculator.
 *
 * <p>Реализует паттерн Singleton для управления единственным экземпляром
 * подключения к базе данных SQLite. Отвечает за инициализацию структуры БД,
 * создание таблиц и предоставление подключений.</p>
 *
 * <p>База данных содержит следующие таблицы:</p>
 * <ul>
 *   <li><b>users</b> - пользователи системы с хешированными паролями</li>
 *   <li><b>calculation_history</b> - история всех выполненных вычислений</li>
 * </ul>
 *
 * <p>При первом запуске автоматически создается администратор по умолчанию:</p>
 * <ul>
 *   <li>Логин: admin</li>
 *   <li>Пароль: admin</li>
 *   <li>Роль: ADMIN</li>
 * </ul>
 *
 * @author magog-1
 * @version 1.0
 * @since 2025-12-08
 */
public class DatabaseManager {
    /** URL подключения к базе данных SQLite */
    private static final String DB_URL = "jdbc:sqlite:engineering_calc.db";

    /** Единственный экземпляр DatabaseManager (Singleton) */
    private static DatabaseManager instance;

    /**
     * Приватный конструктор для реализации паттерна Singleton.
     * Инициализирует базу данных при создании экземпляра.
     */
    private DatabaseManager() {
        initializeDatabase();
    }

    /**
     * Возвращает единственный экземпляр DatabaseManager.
     *
     * <p>Реализация паттерна Singleton. При первом вызове создает
     * новый экземпляр и инициализирует базу данных.</p>
     *
     * @return единственный экземпляр DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Создает и возвращает новое подключение к базе данных.
     *
     * <p>Каждый вызов создает новое подключение к SQLite БД.
     * Вызывающий код должен закрыть соединение после использования,
     * рекомендуется использовать try-with-resources.</p>
     *
     * <p>Пример использования:</p>
     * <pre>
     * try (Connection conn = DatabaseManager.getInstance().getConnection()) {
     *     // Работа с базой данных
     * }
     * </pre>
     *
     * @return новое подключение к базе данных
     * @throws SQLException если не удалось установить соединение
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Инициализирует структуру базы данных.
     *
     * <p>Создает необходимые таблицы, если они не существуют:</p>
     * <ul>
     *   <li>Таблица users с полями: id, username, password_hash, role, created_at</li>
     *   <li>Таблица calculation_history с полями: id, user_id, calculation_type,
     *       input_data, result_data, created_at</li>
     * </ul>
     *
     * <p>Также создает администратора по умолчанию (admin/admin),
     * если он еще не существует в базе данных.</p>
     *
     * @see BCrypt#hashpw(String, String)
     */
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
