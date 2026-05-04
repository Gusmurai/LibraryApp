package ru.library.libraryapp;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DbConnector {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = DbConnector.class.getResourceAsStream("config.properties")) {
            props.load(is);
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws java.sql.SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }
}