module ru.library.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Библиотеки для логирования и упрощения кода
    requires org.slf4j;
    requires static lombok;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    // Разрешаем JavaFX доступ к пакетам
    opens ru.library.libraryapp to javafx.fxml;
    opens ru.library.libraryapp.controllers to javafx.fxml;
    opens ru.library.libraryapp.domains to javafx.base;

    // Экспортируем пакеты для использования вовне
    exports ru.library.libraryapp;
    exports ru.library.libraryapp.controllers;
}