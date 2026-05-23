package ru.library.libraryapp;

import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class DBHelper {
    private static String dbUrl;
    private static String currentDbUser;
    private static String currentDbPass;

    static {
        try (InputStream is = DBHelper.class.getResourceAsStream("/ru/library/libraryapp/config.properties")) {
            Properties props = new Properties();
            if (is == null) {
                throw new RuntimeException("Файл config.properties не найден в ресурсах!");
            }
            props.load(is);
            dbUrl = props.getProperty("db.url");

            // Загружаем драйвер PostgreSQL
            Class.forName("org.postgresql.Driver");
            log.debug("Конфигурация БД загружена. URL: {}", dbUrl);
        } catch (Exception e) {
            log.error("Критическая ошибка инициализации DBHelper", e);
        }
    }

    /**
     * Вызывается при входе. Проверяет корректность данных и сохраняет их для текущей сессии.
     */
    public static void initConnection(String user, String pass) throws SQLException {
        // Пробуем создать тестовое соединение, чтобы проверить пароль
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("charSet", "UTF-8");

        try (Connection testConn = DriverManager.getConnection(dbUrl, props)) {
            // Если ошибки не возникло - запоминаем учетные данные
            currentDbUser = user;
            currentDbPass = pass;
            log.info("Авторизация успешна. Пользователь: {}", user);
        }
    }

    /**
     * Создает и возвращает НОВОЕ соединение для каждого вызова.
     * Это позволяет использовать try-with-resources в DAO.
     */
    public static Connection getConnection() throws SQLException {
        if (currentDbUser == null || currentDbPass == null) {
            log.error("Попытка доступа к БД без авторизации!");
            throw new SQLException("Соединение не установлено. Пожалуйста, войдите в систему.");
        }

        Properties props = new Properties();
        props.setProperty("user", currentDbUser);
        props.setProperty("password", currentDbPass);

        // Настройки кодировки для корректного отображения ошибок от БД
        props.setProperty("client_encoding", "UTF8");
        props.setProperty("charSet", "UTF-8");

        return DriverManager.getConnection(dbUrl, props);
    }
    public static String getCurrentDbUser() {
        return currentDbUser;
    }
    /**
     * Очищает данные сессии при выходе из системы.
     */
    public static void closeConnection() {
        currentDbUser = null;
        currentDbPass = null;
        log.info("Сессия пользователя в приложении завершена.");
    }
}