package ru.library.libraryapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.dao.ReaderDao;
import ru.library.libraryapp.dao.impl.ReaderDaoImpl;
import ru.library.libraryapp.domains.Reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Контроллер для управления формой читателя.
 * Реализована строгая валидация и локализация всех сообщений.
 */
@Slf4j
public class ReaderFormController {

    @FXML private TextField lastNameField, firstNameField, patronymicField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField passportSeriesField, passportNumberField;
    @FXML private TextField addressField, phoneField;
    @FXML private ImageView photoView;
    @FXML private Button btnSave, btnUploadPhoto, btnDeletePhoto, btnCancel;
    @FXML private Label lblStatus;

    @FXML private ResourceBundle resources;

    private Reader currentReader;
    private byte[] photoBytes;
    private boolean saveSuccessful = false;
    private final ReaderDao readerDao = new ReaderDaoImpl();

    @FXML
    public void initialize() {
        lblStatus.setText("");

        // Подсказки форматов
        phoneField.setPromptText("+7XXXXXXXXXX");
        passportSeriesField.setPromptText(resources.getString("prompt.passportSeriesFormat"));
        passportNumberField.setPromptText(resources.getString("prompt.passportNumberFormat"));

        // Ограничиваем ввод символов прямо в полях формы.
        setupNameValidation(lastNameField);
        setupNameValidation(firstNameField);
        setupNameValidation(patronymicField);
        setupNumericValidation(passportSeriesField, 4);
        setupNumericValidation(passportNumberField, 6);
        setupPhoneValidation(phoneField);

        // Блокируем сохранение, пока обязательные поля пустые.
        btnSave.disableProperty().bind(
                lastNameField.textProperty().isEmpty()
                        .or(firstNameField.textProperty().isEmpty())
                        .or(birthDatePicker.getEditor().textProperty().isEmpty())
                        .or(passportSeriesField.textProperty().isEmpty())
                        .or(passportNumberField.textProperty().isEmpty())
                        .or(addressField.textProperty().isEmpty())
                        .or(phoneField.textProperty().isEmpty())
        );

        // Ограничение ввода в текстовое поле календаря (только цифры и точки)
        birthDatePicker.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9.]*")) {
                birthDatePicker.getEditor().setText(oldVal);
            }
        });
    }

    @FXML
    private void onSaveClick() {
        log.info("Попытка сохранения данных читателя...");
        lblStatus.setText(""); // Очищаем статус только при новом клике
        StringBuilder errorMsg = new StringBuilder();

        // Проверяем дату рождения.
        LocalDate birthday = birthDatePicker.getValue();
        String dateText = birthDatePicker.getEditor().getText();

        if (birthday == null && !dateText.isEmpty()) {
            // Если дата не распознана, показываем ошибку формата.
            errorMsg.append(resources.getString("error.dateFormat")).append("\n");
        } else if (birthday != null) {
            // Если дата корректна по формату, проверяем логику
            if (birthday.isAfter(LocalDate.now())) {
                errorMsg.append(resources.getString("error.dateFuture")).append("\n");
            } else if (birthday.isBefore(LocalDate.now().minusYears(120))) {
                errorMsg.append(resources.getString("error.dateInvalid")).append("\n");
            }
        }

        // Проверяем телефон и паспортные данные.
        if (!phoneField.getText().matches("^\\+7\\d{10}$")) {
            errorMsg.append(resources.getString("error.phoneFormat")).append("\n");
        }
        if (passportSeriesField.getText().length() != 4 || passportNumberField.getText().length() != 6) {
            errorMsg.append(resources.getString("error.passportFormat")).append("\n");
        }

        // Вывод ошибок валидации
        if (errorMsg.length() > 0) {
            log.warn("Валидация формы не прошла: {}", errorMsg.toString().replace("\n", " "));
            lblStatus.setText(errorMsg.toString());
            return;
        }

        // Собираем данные из полей формы.
        if (currentReader == null) {
            currentReader = new Reader();
            currentReader.setActive(true);
        }

        currentReader.setLastName(lastNameField.getText().trim());
        currentReader.setFirstName(firstNameField.getText().trim());
        String patronymic = patronymicField.getText().trim();
        currentReader.setPatronymic(patronymic.isEmpty() ? null : patronymic);
        currentReader.setBirthDate(birthday);
        currentReader.setPassportSeries(passportSeriesField.getText().trim());
        currentReader.setPassportNumber(passportNumberField.getText().trim());
        currentReader.setAddress(addressField.getText().trim());
        currentReader.setPhone(phoneField.getText().trim());
        currentReader.setPhoto(photoBytes);

        // Сохраняем читателя в базе данных.
        try {
            log.debug("Отправка объекта читателя в DAO. Читательский билет: {}", currentReader.getTicketNumber());
            if (currentReader.getTicketNumber() == null) {
                readerDao.add(currentReader);
            } else {
                readerDao.update(currentReader);
            }
            this.saveSuccessful = true;
            log.info("Данные читателя успешно сохранены в БД.");
            closeWindow();
        } catch (Exception e) {
            log.error("Ошибка сохранения в базу данных: {}", e.getMessage());
            handleDatabaseError(e.getMessage());
        }
    }

    private void handleDatabaseError(String dbError) {
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
            btnUploadPhoto.setManaged(false);
            btnDeletePhoto.setVisible(false);
            btnDeletePhoto.setManaged(false);
            btnSave.setVisible(false);
            btnSave.setManaged(false);
            lblStatus.setManaged(false);
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
        log.info("Открытие диалога выбора фотографии.");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("window.title.choosePhoto"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(resources.getString("filter.images"), "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(btnCancel.getScene().getWindow());

        if (selectedFile != null) {
            log.info("Выбрано изображение: {}", selectedFile.getName());
            try {
                photoBytes = Files.readAllBytes(selectedFile.toPath());
                photoView.setImage(new Image(new ByteArrayInputStream(photoBytes)));
            } catch (Exception e) {
                lblStatus.setText(resources.getString("error.uploadPhoto"));
            }
        } else {
            log.debug("Выбор фото отменен.");

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
