package ru.library.libraryapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.FineDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.dao.impl.FineDaoImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
/**
 * Контроллер формы оформления штрафа читателю.
 */

@Slf4j
public class FineFormController {
    @FXML private Label lblFineTitle;
    @FXML private ComboBox<String> fineArticleCombo;
    @FXML private TextField fineAmountField;
    @FXML private CheckBox isPaidImmediately;
    @FXML private TextArea fineNoteArea;
    @FXML private Button btnCancelFine, btnConfirmFine;
    @FXML private ResourceBundle resources;

    private Integer lendingId;
    private Integer articleId;
    private boolean saveSuccessful = false;
    private final FineDao fineDao = new FineDaoImpl();

    public void initFineData(Integer lendingId, Integer articleId, String articleName, Double amount) {
        initFineData(lendingId, articleId, articleName, amount, false);
    }

    public void initFineData(Integer lendingId, Integer articleId, String articleName, Double amount, boolean paidImmediately) {
        this.lendingId = lendingId;
        this.articleId = articleId;

        lblFineTitle.setText(String.format(resources.getString("label.fineTitle"), articleName));
        fineArticleCombo.getItems().setAll(articleName);
        fineArticleCombo.setValue(articleName);

        double calculatedAmount = amount != null ? amount : calculateAmount(lendingId, articleId);
        fineAmountField.setText(String.format("%.2f", calculatedAmount).replace(',', '.'));
        fineAmountField.setEditable(isAmountEditable(articleId));
        fineAmountField.setDisable(!isAmountEditable(articleId));
        isPaidImmediately.setSelected(paidImmediately);
        fineNoteArea.clear();
    }

    @FXML
    public void initialize() {
        fineAmountField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("^\\d*([.]\\d*)?$")) {
                fineAmountField.setText(oldValue);
            }
        });
        btnConfirmFine.setOnAction(e -> handleConfirm());
        btnCancelFine.setOnAction(e -> closeWindow());
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }

    private void handleConfirm() {
        double amount;
        try {
            amount = Double.parseDouble(fineAmountField.getText().trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showError(resources.getString("error.invalidFineAmount"));
            return;
        }

        String comment = fineNoteArea.getText() == null ? "" : fineNoteArea.getText().trim();
        boolean paid = isPaidImmediately.isSelected();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        attachOwner(confirm);
        confirm.setTitle(resources.getString("alert.confirm.title"));
        confirm.setHeaderText(lblFineTitle.getText());
        confirm.setContentText(String.format(resources.getString("text.confirmCreateFine"), amount));
        localizeConfirmButtons(confirm);
        if (confirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }

        try {
            fineDao.createFine(lendingId, articleId, amount, comment, paid);
            log.info("Создан штраф. Выдача={}, статья={}, сумма={}, оплачен сразу={}.",
                    lendingId, articleId, amount, paid);
            saveSuccessful = true;
            showSuccess(resources.getString("success.fineCreated"));
            closeWindow();
        } catch (Exception ex) {
            log.error("Не удалось создать штраф. Выдача={}, статья={}: {}",
                    lendingId, articleId, ex.getMessage());
            showError(String.format(resources.getString("error.fineCreateFailed"), ex.getMessage()));
        }
    }

    private boolean isAmountEditable(Integer articleId) {
        return articleId != null && articleId == 2;
    }

    private double calculateAmount(Integer lendingId, Integer articleId) {
        Integer daysOverdue = articleId != null && articleId == 1 ? calculateOverdueDays(lendingId) : null;
        double amount = fineDao.calculateFineAmount(lendingId, articleId, daysOverdue);
        log.debug("Рассчитана сумма штрафа. Выдача={}, статья={}, дней просрочки={}, сумма={}.",
                lendingId, articleId, daysOverdue, amount);
        return amount;
    }

    private Integer calculateOverdueDays(Integer lendingId) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.overdueDays"))) {
            ps.setInt(1, lendingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.warn("Не удалось рассчитать дни просрочки для выдачи {}: {}", lendingId, e.getMessage());
            return 0;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        attachOwner(alert);
        alert.setTitle(resources.getString("alert.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        attachOwner(alert);
        alert.setTitle(resources.getString("alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void localizeConfirmButtons(Alert alert) {
        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        Button cancel = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (ok != null) ok.setText(resources.getString("button.ok"));
        if (cancel != null) cancel.setText(resources.getString("button.cancel"));
    }

    private void attachOwner(javafx.scene.control.Dialog<?> dialog) {
        Window owner = getOwnerWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

    private Window getOwnerWindow() {
        return btnConfirmFine != null && btnConfirmFine.getScene() != null
                ? btnConfirmFine.getScene().getWindow()
                : null;
    }

    private void closeWindow() {
        Stage stage = (Stage) btnConfirmFine.getScene().getWindow();
        stage.close();
    }
}
