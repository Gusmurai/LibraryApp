module ru.library.libraryapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.library.libraryapp to javafx.fxml;
    exports ru.library.libraryapp;
    exports ru.library.libraryapp.controllers;
    opens ru.library.libraryapp.controllers to javafx.fxml;
}