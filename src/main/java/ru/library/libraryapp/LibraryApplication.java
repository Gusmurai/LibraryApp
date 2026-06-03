package ru.library.libraryapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Main JavaFX application class.
 * Loads configuration, initializes localization and switches between login and main windows.
 */
@Slf4j
public class LibraryApplication extends Application {

    private static Stage primaryStage;
    private static ResourceBundle bundle;
    private static final Properties config = new Properties();

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        log.info("Запуск приложения.");

        try (InputStream is = getClass().getResourceAsStream("/ru/library/libraryapp/config.properties")) {
            if (is == null) {
                log.error("Файл config.properties не найден.");
                throw new RuntimeException("config.properties was not found.");
            }
            config.load(is);
        }

        String lang = config.getProperty("app.language", "ru");
        String country = config.getProperty("app.country", "RU");
        Locale locale = new Locale(lang, country);
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("ru.library.libraryapp.messages", locale);

        showLoginView();
    }

    /**
     * Shows the database-authentication window.
     */
    public static void showLoginView() throws Exception {
        log.info("Открыто окно входа.");
        FXMLLoader loader = new FXMLLoader(
                LibraryApplication.class.getResource("/ru/library/libraryapp/login-view.fxml"),
                bundle
        );
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle(bundle.getString("app.title") + " - " + bundle.getString("login.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Shows the main library workspace.
     */
    public static void showMainView() throws Exception {
        log.info("Открыто главное окно библиотеки.");
        FXMLLoader loader = new FXMLLoader(
                LibraryApplication.class.getResource("/ru/library/libraryapp/library-view.fxml"),
                bundle
        );
        Scene scene = new Scene(loader.load(), 1300, 750);

        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Closes the database session when the application stops.
     */
    @Override
    public void stop() {
        log.info("Завершение приложения. Закрытие сеанса БД.");
        DBHelper.closeConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}
