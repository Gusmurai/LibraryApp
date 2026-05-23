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
 * Основной класс приложения.
 * Реализует логику переключения между окном авторизации и основным интерфейсом.
 */
@Slf4j
public class LibraryApplication extends Application {

    private static Stage primaryStage;
    private static ResourceBundle bundle;
    private static Properties config = new Properties();

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        log.info("Приложение запускается...");

        // 1. Загрузка общих настроек (URL базы данных и язык)
        try (InputStream is = getClass().getResourceAsStream("/ru/library/libraryapp/config.properties")) {
            if (is == null) {
                log.error("Файл config.properties не найден!");
                throw new RuntimeException("Файл config.properties не найден!");
            }
            config.load(is);
        }

        // 2. Настройка локализации (из файла конфигурации)
        String lang = config.getProperty("app.language", "ru");
        String country = config.getProperty("app.country", "RU");
        Locale locale = new Locale(lang, country);
        Locale.setDefault(locale);

        // Загружаем тексты интерфейса
        bundle = ResourceBundle.getBundle("ru.library.libraryapp.messages", locale);

        // 3. ПЕРВЫМ ПОКАЗЫВАЕМ ОКНО АВТОРИЗАЦИИ
        showLoginView();
    }

    /**
     * Отображает окно входа в систему.
     */
    public static void showLoginView() throws Exception {
        log.info("Отображение окна авторизации.");
        // ИСПРАВЛЕНО: getClass() -> LibraryApplication.class
        FXMLLoader loader = new FXMLLoader(
                LibraryApplication.class.getResource("/ru/library/libraryapp/login-view.fxml"),
                bundle
        );
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle(bundle.getString("app.title") + " - Вход");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Отображает основное рабочее окно библиотеки.
     */
    public static void showMainView() throws Exception {
        log.info("Загрузка основного интерфейса системы.");
        // ИСПРАВЛЕНО: getClass() -> LibraryApplication.class
        FXMLLoader loader = new FXMLLoader(
                LibraryApplication.class.getResource("/ru/library/libraryapp/library-view.fxml"),
                bundle
        );
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Выполняется при закрытии приложения.
     */
    @Override
    public void stop() {
        log.info("Приложение закрывается. Завершение сессии БД.");
        DBHelper.closeConnection(); // Закрываем соединение с PostgreSQL
    }

    public static void main(String[] args) {
        launch();
    }
}