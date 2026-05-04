package ru.library.libraryapp.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.library.libraryapp.dao.ReaderDao;
import ru.library.libraryapp.dao.impl.ReaderDaoImpl;
import ru.library.libraryapp.domains.Reader;

import java.io.IOException;
import java.util.Optional;

public class LibraryController {

    @FXML private TextField searchField;
    @FXML private TableView<Reader> readersTable;
    @FXML private TableColumn<Reader, Integer> colTicket;
    @FXML private TableColumn<Reader, String> colLastName;
    @FXML private TableColumn<Reader, String> colFirstName;
    @FXML private TableColumn<Reader, String> colPatronymic;
    @FXML private TableColumn<Reader, String> colPhone;
    @FXML private TableColumn<Reader, String> colStatus;

    @FXML private Button btnArchive;
    @FXML private Button btnRestore;
    @FXML private Button btnEditReader;
    @FXML private Button btnAddReader;
    @FXML private Button btnDetails;
    @FXML private Button btnFormular;
    @FXML private Label lblMainStatus;
    @FXML
    private java.util.ResourceBundle resources;
    private final ReaderDao readerDao = new ReaderDaoImpl();
    private final ObservableList<Reader> readerData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colTicket.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colPatronymic.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    boolean active = getTableRow().getItem().getActive();
                    setText(active ? "Активен" : "В архиве");
                }
            }
        });

        // 2. Первоначальная загрузка данных
        loadReaders();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadReaders();
            } else {
                readerData.setAll(readerDao.searchReaders(newValue));
            }
        });

        readersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                btnEditReader.setDisable(true);
                btnDetails.setDisable(true);
                btnFormular.setDisable(true);
                btnArchive.setDisable(true);
                btnRestore.setDisable(true);
            } else {
                boolean isActive = newSelection.getActive();
                btnEditReader.setDisable(false);
                btnDetails.setDisable(false);
                btnFormular.setDisable(false);

                // Кнопка "В архив" активна только для тех, кто не в архиве
                btnArchive.setDisable(!isActive);
                // Кнопка "Восстановить" активна только для тех, кто в архиве
                btnRestore.setDisable(isActive);
            }
        });

        btnAddReader.setOnAction(event -> openReaderForm(null, false));

        btnEditReader.setOnAction(event -> {
            Reader selected = readersTable.getSelectionModel().getSelectedItem();
            if (selected != null) openReaderForm(selected, false);
        });

        btnDetails.setOnAction(event -> {
            Reader selected = readersTable.getSelectionModel().getSelectedItem();
            if (selected != null) openReaderForm(selected, true);
        });

        btnArchive.setOnAction(event -> handleStatusChange(false));
        btnRestore.setOnAction(event -> handleStatusChange(true));

        btnFormular.setOnAction(event -> {
            Reader selected = readersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Здесь будет вызов метода открытия окна формуляра
            }
        });
    }

    private void loadReaders() {
        readerData.setAll(readerDao.findAll());
        readersTable.setItems(readerData);
    }

    private void handleStatusChange(boolean moveToActive) {
        Reader selected = readersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String fullName = selected.getLastName() + " " + selected.getFirstName();

        // ЛОКАЛИЗАЦИЯ ВОПРОСА
        String confirmMsg = moveToActive ? resources.getString("alert.confirm.restore") : resources.getString("alert.confirm.archive");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(resources.getString("alert.confirm.title"));
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(confirmMsg + " " + fullName + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                readerDao.changeStatus(selected.getTicketNumber(), moveToActive);
                loadReaders();
                showSuccess(resources.getString("success.statusChanged"));
            } catch (Exception e) {
                showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
            }
        }
    }

    private void openReaderForm(Reader reader, boolean isViewOnly) {
        try {
            // Используем bundle из LibraryApplication или загружаем заново для этого окна
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ru.library.libraryapp.messages", java.util.Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/library/libraryapp/reader-form.fxml"), bundle);
            Parent root = loader.load();

            ReaderFormController controller = loader.getController();
            controller.setReaderData(reader, isViewOnly);

            Stage stage = new Stage();

            // ЛОКАЛИЗАЦИЯ ЗАГОЛОВКА ОКНА
            String titleKey = isViewOnly ? "window.title.detailsReader" : (reader == null ? "window.title.addReader" : "window.title.editReader");
            stage.setTitle(resources.getString(titleKey));

            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (!isViewOnly && controller.isSaveSuccessful()) {
                loadReaders();
                Reader savedReader = controller.getReader();
                String fullName = savedReader.getLastName() + " " + savedReader.getFirstName();

                // ЛОКАЛИЗАЦИЯ СООБЩЕНИЯ ОБ УСПЕХЕ
                String msg = (reader == null ? resources.getString("success.added") : resources.getString("success.updated")) + fullName;
                showSuccess(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert(resources.getString("alert.error.interfaceTitle"), resources.getString("alert.error.openForm"));
        }
    }

    private void showSuccess(String message) {
        lblMainStatus.setText(message);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> lblMainStatus.setText(""));
            } catch (InterruptedException e) {
            }
        }).start();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}