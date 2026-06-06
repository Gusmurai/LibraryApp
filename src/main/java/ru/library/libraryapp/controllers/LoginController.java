package ru.library.libraryapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.LibraryApplication;

import java.sql.SQLException;
import java.util.ResourceBundle;
/**
 * Контроллер окна входа, выполняющий аутентификацию через механизмы СУБД
 */


@Slf4j
public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    @FXML private ResourceBundle resources; // Сюда JavaFX подставит текущий язык

    @FXML
    public void initialize() {
        // Вход по нажатию клавиши ввода в поле пароля
        passwordField.setOnAction(event -> onLoginClick());

        // Скрываем ошибку, когда пользователь начинает заново вводить данные
        loginField.textProperty().addListener((obs, old, newVal) -> lblError.setVisible(false));
        passwordField.textProperty().addListener((obs, old, newVal) -> lblError.setVisible(false));



    }

    @FXML
    private void onLoginClick() {
        String user = loginField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText(resources.getString("login.error.empty"));
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
                lblError.setText(resources.getString("login.error.ui"));
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            String clearMessage;

            // Показываем понятное сообщение при ошибке входа в СУБД.
            if (isAuthenticationError(e)) {
                clearMessage = resources.getString("login.error.auth");
            } else {
                // Приводим системное сообщение к читаемому виду.
                try {
                    String rawMessage = e.getMessage();
                    clearMessage = new String(rawMessage.getBytes("ISO-8859-1"), "Windows-1251");
                } catch (Exception ex) {
                    clearMessage = e.getMessage();
                }
                clearMessage = resources.getString("login.error.dbPrefix") + clearMessage;
            }

            log.warn("Ошибка входа (Код {}): {}", sqlState, clearMessage);
            lblError.setText(clearMessage);
            lblError.setVisible(true);
        }
    }

    private boolean isAuthenticationError(SQLException e) {
        String sqlState = e.getSQLState();
        // Коды, начинающиеся на 28 — это стандартные ошибки аутентификации в SQL
        return sqlState != null && sqlState.startsWith("28");
    }
}
