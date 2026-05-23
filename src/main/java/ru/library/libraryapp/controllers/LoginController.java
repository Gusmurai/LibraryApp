package ru.library.libraryapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.LibraryApplication;

import java.sql.SQLException;



@Slf4j
public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    @FXML
    public void initialize() {
        System.setProperty("file.encoding", "UTF-8");
        // Вход по нажатию Enter в поле пароля (удобство для пользователя)
        passwordField.setOnAction(event -> onLoginClick());

    }

    @FXML
    private void onLoginClick() {
        String user = loginField.getText().trim();
        String pass = passwordField.getText();

        // Простая проверка на пустые поля перед обращением к БД
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Заполните все поля");
            lblError.setVisible(true);
            return;
        }

        try {

            DBHelper.initConnection(user, pass);

            log.info("Пользователь {} успешно вошел в систему.", user);

            try {
                LibraryApplication.showMainView();
            } catch (Exception e) {
                log.error("Критическая ошибка при загрузке главного окна", e);
                lblError.setText("Ошибка интерфейса. Проверьте логи.");
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            String clearMessage;

            // 28P01 - неверный пароль, 28000 - пользователь не найден
            if ("28P01".equals(sqlState) || "28000".equals(sqlState)) {
                clearMessage = "Неверный логин или пароль";
            } else {
                // Если кодировка всё еще сломана, попробуем перекодировать вручную для лога
                try {
                    clearMessage = new String(e.getMessage().getBytes("ISO-8859-1"), "Windows-1251");
                } catch (Exception ex) {
                    clearMessage = e.getMessage();
                }
            }

            log.warn("Ошибка входа (Код {}): {}", sqlState, clearMessage);

            lblError.setText(clearMessage);
            lblError.setVisible(true);

        }
    }
}