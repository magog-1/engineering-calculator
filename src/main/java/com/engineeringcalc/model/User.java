package com.engineeringcalc.model;

import com.engineeringcalc.database.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * Класс модели пользователя приложения Engineering Calculator.
 *
 * <p>Представляет пользователя системы с его учетными данными и ролью.
 * Поддерживает операции аутентификации и регистрации пользователей
 * с использованием безопасного хеширования паролей BCrypt.</p>
 *
 * <p>Доступные роли пользователей:</p>
 * <ul>
 *   <li>USER - обычный пользователь с базовыми правами</li>
 *   <li>ADMIN - администратор с расширенными правами</li>
 * </ul>
 *
 * @author magog-1
 * @version 1.0
 * @since 2025-12-08
 * @see DatabaseManager
 */
public class User {
    /** Уникальный идентификатор пользователя в базе данных */
    private int id;

    /** Имя пользователя для входа в систему */
    private String username;

    /** Роль пользователя (USER или ADMIN) */
    private String role;

    /**
     * Создает нового пользователя с указанными параметрами.
     *
     * @param id уникальный идентификатор пользователя
     * @param username имя пользователя
     * @param role роль пользователя ("USER" или "ADMIN")
     */
    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public int getId() {
        return id;
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return имя пользователя
     */
    public String getUsername() {
        return username;
    }

    /**
     * Возвращает роль пользователя.
     *
     * @return роль пользователя ("USER" или "ADMIN")
     */
    public String getRole() {
        return role;
    }

    /**
     * Проверяет, является ли пользователь администратором.
     *
     * @return true, если пользователь имеет роль ADMIN, иначе false
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Выполняет аутентификацию пользователя по имени и паролю.
     *
     * <p>Метод проверяет учетные данные пользователя в базе данных,
     * сравнивая введенный пароль с сохраненным хешем BCrypt.
     * В случае успешной аутентификации возвращает объект User.</p>
     *
     * @param username имя пользователя для входа
     * @param password пароль пользователя (будет проверен с хешем)
     * @return объект User при успешной аутентификации, null при неудаче
     *
     * @see BCrypt#checkpw(String, String)
     * @see DatabaseManager#getConnection()
     */
    public static User authenticate(String username, String password) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, password_hash, role FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, storedHash)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * <p>Создает нового пользователя с указанным именем, паролем и ролью.
     * Пароль хешируется с использованием BCrypt перед сохранением в базе данных.
     * Имя пользователя должно быть уникальным.</p>
     *
     * @param username уникальное имя пользователя (не должно существовать в БД)
     * @param password пароль пользователя (будет захеширован перед сохранением)
     * @param role роль нового пользователя ("USER" или "ADMIN")
     * @return true при успешной регистрации, false при ошибке
     *
     * @see BCrypt#hashpw(String, String)
     * @see BCrypt#gensalt()
     */
    public static boolean register(String username, String password, String role) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)")) {

            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
