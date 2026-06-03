package ru.library.libraryapp;

import javafx.application.Application;
/**
 * Точка запуска приложения для сборок, где требуется отдельный стартовый класс.
 */

public class Launcher {
    public static void main(String[] args) {
        Application.launch(LibraryApplication.class, args);
    }
}
