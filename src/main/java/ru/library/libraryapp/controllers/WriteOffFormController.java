package ru.library.libraryapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.dao.WriteOffDao;
import ru.library.libraryapp.dao.WriteOffReasonDao;
import ru.library.libraryapp.dao.impl.WriteOffDaoImpl;
import ru.library.libraryapp.dao.impl.WriteOffReasonDaoImpl;
import ru.library.libraryapp.domains.WriteOff;
import ru.library.libraryapp.domains.WriteOffReason;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
public class WriteOffFormController {
    @FXML private Label lblWriteOffInv, lblWriteOffBook;
    @FXML private ComboBox<WriteOffReason> reasonCombo;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private ResourceBundle resources;

    private Integer currentInvNumber;
    private Integer librarianId;
    private boolean allowReaderLossReason;
    private boolean saveSuccessful = false;
    private List<WriteOffReason> allReasons = List.of();

    private final WriteOffDao writeOffDao = new WriteOffDaoImpl();
    private final WriteOffReasonDao reasonDao = new WriteOffReasonDaoImpl();

    @FXML
    public void initialize() {
        reasonCombo.setCellFactory(list -> reasonCell());
        reasonCombo.setButtonCell(reasonCell());
        allReasons = reasonDao.findAll();
        refreshReasons();
    }

    public void setLibrarianId(Integer librarianId) {
        if (librarianId != null) {
            this.librarianId = librarianId;
        }
    }

    public void initWriteOffData(int invNumber, String bookTitle, String defaultReason, boolean lockCancel) {
        this.currentInvNumber = invNumber;
        this.allowReaderLossReason = lockCancel;
        refreshReasons();
        lblWriteOffInv.setText(String.format(resources.getString("text.writeOffInv"), invNumber));
        lblWriteOffBook.setText(String.format(resources.getString("text.writeOffBook"), bookTitle));
        selectReason(defaultReason);
        if (lockCancel && btnCancel != null) {
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            reasonCombo.setDisable(true);
        }
    }

    @FXML
    private void onSaveClick() {
        WriteOffReason reason = reasonCombo.getSelectionModel().getSelectedItem();
        if (librarianId == null) {
            showError(resources.getString("error.librarianBindingRequired"));
            return;
        }
        if (currentInvNumber == null || reason == null) {
            showError(resources.getString("error.reasonRequired"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        attachOwner(confirm);
        confirm.setTitle(resources.getString("alert.confirm.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(resources.getString("text.confirmWriteOff"));
        Button ok = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        Button cancel = (Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (ok != null) ok.setText(resources.getString("button.writeOff"));
        if (cancel != null) cancel.setText(resources.getString("button.cancel"));
        if (confirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }

        try {
            WriteOff writeOff = new WriteOff();
            writeOff.setInventoryNumber(currentInvNumber);
            writeOff.setReasonId(reason.getReasonId());
            writeOff.setTabelNumber(librarianId);
            writeOffDao.create(writeOff);
            log.info("Write-off created. Inventory={}, librarian={}, reason={}.",
                    currentInvNumber, librarianId, reason.getName());
            saveSuccessful = true;
            closeWindow();
        } catch (Exception e) {
            log.error("Failed to create write-off for inventory {}.", currentInvNumber, e);
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCancelClick() {
        closeWindow();
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }

    private void selectReason(String defaultReason) {
        List<WriteOffReason> reasons = reasonCombo.getItems();
        if (defaultReason != null && !defaultReason.isBlank()) {
            reasons.stream()
                    .filter(reason -> defaultReason.equalsIgnoreCase(reason.getName()))
                    .findFirst()
                    .ifPresent(reason -> reasonCombo.getSelectionModel().select(reason));

            if (reasonCombo.getSelectionModel().isEmpty() && isReaderLossText(defaultReason)) {
                reasons.stream()
                        .filter(this::isReaderLossReason)
                        .findFirst()
                        .ifPresent(reason -> reasonCombo.getSelectionModel().select(reason));
            }
        }
        if (reasonCombo.getSelectionModel().isEmpty() && !reasons.isEmpty()) {
            reasonCombo.getSelectionModel().selectFirst();
        }
    }

    private void refreshReasons() {
        reasonCombo.getItems().setAll(allReasons.stream()
                .filter(reason -> allowReaderLossReason || !isReaderLossReason(reason))
                .toList());
    }

    private boolean isReaderLossReason(WriteOffReason reason) {
        return reason != null && isReaderLossText(reason.getName());
    }

    private boolean isReaderLossText(String text) {
        if (text == null) {
            return false;
        }
        String name = text.toLowerCase();
        return name.contains("\u0443\u0442\u0440\u0430\u0442")
                || name.contains("\u0443\u0442\u0435\u0440")
                || name.contains("loss")
                || name.contains("lost");
    }

    private ListCell<WriteOffReason> reasonCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(WriteOffReason item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || Objects.isNull(item) ? null : item.getName());
            }
        };
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        attachOwner(alert);
        alert.setTitle(resources.getString("alert.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void attachOwner(javafx.scene.control.Dialog<?> dialog) {
        Window owner = getOwnerWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

    private Window getOwnerWindow() {
        return btnSave != null && btnSave.getScene() != null
                ? btnSave.getScene().getWindow()
                : null;
    }

    private void closeWindow() {
        ((Stage) btnSave.getScene().getWindow()).close();
    }
}
