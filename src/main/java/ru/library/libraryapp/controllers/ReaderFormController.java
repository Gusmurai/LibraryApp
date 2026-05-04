package ru.library.libraryapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.library.libraryapp.dao.ReaderDao;
import ru.library.libraryapp.dao.impl.ReaderDaoImpl;
import ru.library.libraryapp.domains.Reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ResourceBundle;

/**
 * Контроллер для управления формой читателя (регистрация, редактирование, просмотр).
 * Поддерживает полную локализацию и валидацию данных.
 */
public class ReaderFormController {

    @FXML private TextField lastNameField, firstNameField, patronymicField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField passportSeriesField, passportNumberField;
    @FXML private TextField addressField, phoneField;
    @FXML private ImageView photoView;
    @FXML private Button btnSave, btnUploadPhoto, btnDeletePhoto, btnCancel;
    @FXML private Label lblStatus;

    @FXML private ResourceBundle resources; // Автоматически внедряется FXMLLoader-ом

    private Reader currentReader;
    private byte[] photoBytes;
    private boolean saveSuccessful = false;
    private final ReaderDao readerDao = new ReaderDaoImpl();

    @FXML
    public void initialize() {
        // Очистка статуса
        lblStatus.setText("");

        // Ограничения ввода на лету (Лабораторная 7)
        setupNameValidation(lastNameField);
        setupNameValidation(firstNameField);
        setupNameValidation(patronymicField);
        setupNumericValidation(passportSeriesField, 4);
        setupNumericValidation(passportNumberField, 6);
        setupPhoneValidation(phoneField);

        // Блокировка кнопки сохранения (Поля обязательны)
        btnSave.disableProperty().bind(
                lastNameField.textProperty().isEmpty()
                        .or(firstNameField.textProperty().isEmpty())
                        .or(birthDatePicker.valueProperty().isNull())
                        .or(passportSeriesField.textProperty().isEmpty())
                        .or(passportNumberField.textProperty().isEmpty())
                        .or(addressField.textProperty().isEmpty())
                        .or(phoneField.textProperty().isEmpty())
        );

        // Сброс текста ошибки при изменении данных
        lastNameField.textProperty().addListener((obs, old, newVal) -> lblStatus.setText(""));
        firstNameField.textProperty().addListener((obs, old, newVal) -> lblStatus.setText(""));
        phoneField.textProperty().addListener((obs, old, newVal) -> lblStatus.setText(""));

        // Запрет букв в редакторе даты
        birthDatePicker.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9.]*")) {
                birthDatePicker.getEditor().setText(oldVal);
            }
        });
    }

    @FXML
    private void onSaveClick() {
        lblStatus.setText("");
        StringBuilder errorMsg = new StringBuilder();

        // 1. Проверка корректности даты (если введена вручную неверно)
        if (birthDatePicker.getValue() == null && !birthDatePicker.getEditor().getText().isEmpty()) {
            lblStatus.setText(resources.getString("error.dateFormat"));
            return;
        }

        // 2. Валидация форматов (Лабораторная 7)
        if (!phoneField.getText().matches("^\\+7\\d{10}$")) {
            errorMsg.append(resources.getString("error.phoneFormat")).append("\n");
        }
        if (passportSeriesField.getText().length() != 4 || passportNumberField.getText().length() != 6) {
            errorMsg.append(resources.getString("error.passportFormat")).append("\n");
        }

        if (errorMsg.length() > 0) {
            lblStatus.setText(errorMsg.toString());
            return;
        }

        // 3. Сбор данных
        if (currentReader == null) {
            currentReader = new Reader();
            currentReader.setActive(true);
        }

        currentReader.setLastName(lastNameField.getText().trim());
        currentReader.setFirstName(firstNameField.getText().trim());
        String patronymic = patronymicField.getText().trim();
        currentReader.setPatronymic(patronymic.isEmpty() ? null : patronymic);
        currentReader.setBirthDate(birthDatePicker.getValue());
        currentReader.setPassportSeries(passportSeriesField.getText().trim());
        currentReader.setPassportNumber(passportNumberField.getText().trim());
        currentReader.setAddress(addressField.getText().trim());
        currentReader.setPhone(phoneField.getText().trim());
        currentReader.setPhoto(photoBytes);

        // 4. Попытка сохранения в БД
        try {
            if (currentReader.getTicketNumber() == null) {
                readerDao.add(currentReader);
            } else {
                readerDao.update(currentReader);
            }
            this.saveSuccessful = true;
            closeWindow();
        } catch (Exception e) {
            // Локализация системных ошибок (из триггеров БД)
            String dbError = e.getMessage();
            if (dbError.contains("uq_passport") || dbError.contains("uq_reader_passport")) {
                lblStatus.setText(resources.getString("error.duplicatePassport"));
            } else if (dbError.contains("смешивать русские и английские")) {
                lblStatus.setText(resources.getString("error.mixedLanguages"));
            } else if (dbError.contains("недопустимые символы")) {
                lblStatus.setText(resources.getString("error.invalidChars"));
            } else {
                lblStatus.setText(resources.getString("error.dbOther") + dbError);
            }
        }
    }

    public void setReaderData(Reader reader, boolean isViewOnly) {
        this.currentReader = reader;
        if (reader != null) {
            lastNameField.setText(reader.getLastName());
            firstNameField.setText(reader.getFirstName());
            patronymicField.setText(reader.getPatronymic() != null ? reader.getPatronymic() : "");
            birthDatePicker.setValue(reader.getBirthDate());
            passportSeriesField.setText(reader.getPassportSeries());
            passportNumberField.setText(reader.getPassportNumber());
            addressField.setText(reader.getAddress());
            phoneField.setText(reader.getPhone());

            if (reader.getPhoto() != null && reader.getPhoto().length > 0) {
                this.photoBytes = reader.getPhoto();
                photoView.setImage(new Image(new ByteArrayInputStream(this.photoBytes)));
            }
        }

        if (isViewOnly) {
            setFieldsEditable(false);
            btnUploadPhoto.setVisible(false);
            btnDeletePhoto.setVisible(false);
            btnSave.setVisible(false);
            btnCancel.setText(resources.getString("button.close"));
        }
    }

    private void setFieldsEditable(boolean value) {
        lastNameField.setEditable(value);
        firstNameField.setEditable(value);
        patronymicField.setEditable(value);
        passportSeriesField.setEditable(value);
        passportNumberField.setEditable(value);
        addressField.setEditable(value);
        phoneField.setEditable(value);
        birthDatePicker.setDisable(!value);
        birthDatePicker.setStyle("-fx-opacity: 1;");
        birthDatePicker.getEditor().setStyle("-fx-opacity: 1;");
    }

    @FXML
    private void onUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("window.title.choosePhoto"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(resources.getString("filter.images"), "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(btnCancel.getScene().getWindow());

        if (selectedFile != null) {
            try {
                photoBytes = Files.readAllBytes(selectedFile.toPath());
                photoView.setImage(new Image(new ByteArrayInputStream(photoBytes)));
            } catch (Exception e) {
                lblStatus.setText(resources.getString("error.uploadPhoto"));
            }
        }
    }

    @FXML private void onDeletePhoto() {
        photoBytes = null;
        photoView.setImage(null);
    }

    @FXML private void onCancelClick() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void setupNameValidation(TextField field) {
        field.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("[А-Яа-яЁё\\s-]*")) field.setText(old);
        });
    }

    private void setupNumericValidation(TextField field, int maxLength) {
        field.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*") || newValue.length() > maxLength) field.setText(old);
        });
    }

    private void setupPhoneValidation(TextField field) {
        field.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty()) {
                if (newVal.length() > 12) { field.setText(old); return; }
                if (!newVal.matches("\\+?\\d*")) { field.setText(old); }
            }
        });
    }

    public boolean isSaveSuccessful() { return saveSuccessful; }
    public Reader getReader() { return currentReader; }
}