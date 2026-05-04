package ru.library.libraryapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class LibraryApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Загрузка настроек
        Properties config = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/ru/library/libraryapp/config.properties")) {
            if (is == null) {
                throw new RuntimeException("Файл config.properties не найден!");
            }
            config.load(is);
        }

        // Локализация
        String lang = config.getProperty("app.language", "ru");
        String country = config.getProperty("app.country", "RU");
        Locale locale = new Locale(lang, country);

        Locale.setDefault(locale);

        // Файл с переводами
        ResourceBundle bundle = ResourceBundle.getBundle("ru.library.libraryapp.messages", locale);


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ru/library/libraryapp/library-view.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle(bundle.getString("app.title"));
        stage.setScene(scene);

        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}