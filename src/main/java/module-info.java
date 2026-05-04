module ru.library.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    opens ru.library.libraryapp to javafx.fxml;
    opens ru.library.libraryapp.domains to javafx.base;
    exports ru.library.libraryapp;
    exports ru.library.libraryapp.controllers;
    opens ru.library.libraryapp.controllers to javafx.fxml;
}