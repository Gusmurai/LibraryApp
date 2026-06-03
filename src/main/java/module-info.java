module ru.library.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.slf4j;
    requires static lombok;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    opens ru.library.libraryapp to javafx.fxml;
    opens ru.library.libraryapp.controllers to javafx.fxml;
    opens ru.library.libraryapp.domains to javafx.base;

    exports ru.library.libraryapp;
    exports ru.library.libraryapp.controllers;
}
