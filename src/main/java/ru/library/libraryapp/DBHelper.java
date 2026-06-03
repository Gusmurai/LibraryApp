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
                throw new IllegalStateException("config.properties was not found in application resources.");
            }
            props.load(is);
            dbUrl = props.getProperty("db.url");
            Class.forName("org.postgresql.Driver");
            log.debug("Загружена конфигурация подключения к БД. URL: {}", dbUrl);
        } catch (Exception e) {
            log.error("Ошибка инициализации DBHelper.", e);
        }
    }

    public static void initConnection(String user, String pass) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("charSet", "UTF-8");

        try (Connection ignored = DriverManager.getConnection(dbUrl, props)) {
            currentDbUser = user;
            currentDbPass = pass;
            log.info("Аутентификация в БД успешно выполнена для пользователя {}.", user);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (currentDbUser == null || currentDbPass == null) {
            log.error("Запрошен доступ к БД до прохождения аутентификации.");
            throw new SQLException("Database connection is not initialized. Please sign in first.");
        }

        Properties props = new Properties();
        props.setProperty("user", currentDbUser);
        props.setProperty("password", currentDbPass);
        props.setProperty("client_encoding", "UTF8");
        props.setProperty("charSet", "UTF-8");
        return DriverManager.getConnection(dbUrl, props);
    }

    public static String getCurrentDbUser() {
        return currentDbUser;
    }

    public static void closeConnection() {
        currentDbUser = null;
        currentDbPass = null;
        log.info("Сеанс подключения приложения к БД закрыт.");
    }
}
