package ru.library.libraryapp.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.dao.BookDao;
import ru.library.libraryapp.dao.CopyDao;
import ru.library.libraryapp.dao.impl.BookDaoImpl;
import ru.library.libraryapp.dao.impl.CopyDaoImpl;
import ru.library.libraryapp.domains.Book;
import ru.library.libraryapp.domains.Copy;

import java.io.ByteArrayInputStream;
import java.util.ResourceBundle;/**
 * Контроллер окна выбора книги или конкретного экземпляра для выдачи и бронирования.
 */


@Slf4j
public class BookSelectController {

    // Таблица всех книг каталога.
    @FXML private TextField searchBookSelect;
    @FXML private TableView<Book> bookSelectionTable;
    @FXML private TableColumn<Book, String> colSelIsbn, colSelTitle, colSelAuthor, colSelTotal;

    // Таблица доступных экземпляров выбранной книги.
    @FXML private TextField searchInvInSelect;
    @FXML private TableView<Copy> copySelectionTable;
    @FXML private TableColumn<Copy, Integer> colSelInv;
    @FXML private TableColumn<Copy, Double> colSelPrice;

    @FXML private ImageView bookSelectCover;
    @FXML private Label lblSelTitle, lblSelAuthors, lblSelPublisher, lblSelYear,
            lblSelPages, lblSelGenres, lblSelIsbn, lblSelBbk, lblSelAuthorMark;
    @FXML private Label labelAvailableCopies;
    @FXML private HBox invSearchBox;

    @FXML private Button btnFinalSelect;
    @FXML private ResourceBundle resources;

    private Integer resultInventoryNumber = null;
    private Book selectedBook;
    private String preselectedIsbn;
    private boolean bookOnlyMode = false;
    private final BookDao bookDao = new BookDaoImpl();
    private final CopyDao copyDao = new CopyDaoImpl();
    private final ObservableList<Book> bookData = FXCollections.observableArrayList();
    private final ObservableList<Copy> copyData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настраиваем таблицу каталога.
        colSelIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colSelTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSelAuthor.setCellValueFactory(new PropertyValueFactory<>("authors"));
        colSelTotal.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));

        // Настраиваем таблицу экземпляров.
        colSelInv.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        colSelPrice.setCellValueFactory(new PropertyValueFactory<>("cost"));

        loadAllBooks("");

        // Поиск сразу обновляет список книг.
        searchBookSelect.textProperty().addListener((obs, old, newVal) -> loadAllBooks(newVal));

        // При выборе книги показываем карточку и доступные экземпляры.
        bookSelectionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                updateDetailCard(newVal);
                searchInvInSelect.clear();
                loadOnlyAvailableCopies(newVal.getIsbn(), "");
            }
        });

        // Поиск по инвентарному номеру работает внутри выбранного издания.
        searchInvInSelect.textProperty().addListener((obs, old, newVal) -> {
            Book selected = bookSelectionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadOnlyAvailableCopies(selected.getIsbn(), newVal);
            }
        });

        bindFinalSelectButton();
    }

    public void enableBookOnlyMode() {
        bookOnlyMode = true;
        btnFinalSelect.setText(resources.getString("button.selectThisBook"));
        copySelectionTable.setDisable(true);
        searchInvInSelect.setDisable(true);
        setCopySelectionVisible(false);
        bindFinalSelectButton();
    }

    public void enableCopySelectionMode() {
        bookOnlyMode = false;
        btnFinalSelect.setText(resources.getString("button.selectThisCopy"));
        copySelectionTable.setDisable(false);
        searchInvInSelect.setDisable(false);
        setCopySelectionVisible(true);
        bindFinalSelectButton();
    }

    private void setCopySelectionVisible(boolean visible) {
        if (labelAvailableCopies != null) {
            labelAvailableCopies.setVisible(visible);
            labelAvailableCopies.setManaged(visible);
        }
        if (invSearchBox != null) {
            invSearchBox.setVisible(visible);
            invSearchBox.setManaged(visible);
        }
        if (copySelectionTable != null) {
            copySelectionTable.setVisible(visible);
            copySelectionTable.setManaged(visible);
        }
    }

    private void bindFinalSelectButton() {
        btnFinalSelect.disableProperty().unbind();
        if (bookOnlyMode) {
            btnFinalSelect.disableProperty().bind(bookSelectionTable.getSelectionModel().selectedItemProperty().isNull());
        } else {
            btnFinalSelect.disableProperty().bind(copySelectionTable.getSelectionModel().selectedItemProperty().isNull());
        }
    }

    private void loadAllBooks(String query) {
        // Загружаем все книги каталога, даже если сейчас нет свободных экземпляров.
        bookData.setAll(query.isEmpty() ? bookDao.findAll() : bookDao.searchBooks(query));
        bookSelectionTable.setItems(bookData);
        if (preselectedIsbn != null) {
            selectBookByIsbn(preselectedIsbn);
        }
    }

    public void preselectBook(String isbn) {
        this.preselectedIsbn = normalizeIsbn(isbn);
        if (isbn != null && !isbn.isBlank()) {
            String normalized = normalizeIsbn(isbn);
            searchBookSelect.setText(normalized);
            loadAllBooks(normalized);
            Platform.runLater(() -> selectBookByIsbn(normalized));
        }
    }

    private void selectBookByIsbn(String isbn) {
        String normalized = normalizeIsbn(isbn);
        bookData.stream()
                .filter(book -> normalized.equals(normalizeIsbn(book.getIsbn())))
                .findFirst()
                .ifPresent(book -> {
                    preselectedIsbn = null;
                    bookSelectionTable.getSelectionModel().select(book);
                    bookSelectionTable.scrollTo(book);
                    bookSelectionTable.requestFocus();
                    bookSelectionTable.getFocusModel().focus(bookSelectionTable.getSelectionModel().getSelectedIndex());
                    updateDetailCard(book);
                    loadOnlyAvailableCopies(book.getIsbn(), searchInvInSelect.getText() == null ? "" : searchInvInSelect.getText());
                });
    }

    private String normalizeIsbn(String isbn) {
        return isbn == null ? "" : isbn.trim();
    }

    private void loadOnlyAvailableCopies(String isbn, String invQuery) {
        // Для выдачи показываем только экземпляры со статусом "В наличии".
        copyData.setAll(copyDao.searchAvailable(isbn, invQuery));
        copySelectionTable.setItems(copyData);
        if (!bookOnlyMode && copySelectionTable.getSelectionModel().getSelectedItem() == null && !copyData.isEmpty()) {
            copySelectionTable.getSelectionModel().selectFirst();
        }
    }

    private void updateDetailCard(Book book) {
        Book fullBook = enrichBook(book);
        lblSelTitle.setText(valueOrDash(fullBook.getTitle()));
        lblSelAuthors.setText(valueOrDash(fullBook.getAuthors()));
        lblSelPublisher.setText(valueOrDash(fullBook.getPublisherName()));
        lblSelYear.setText(fullBook.getPublicationYear() != null ? fullBook.getPublicationYear().toString() : "-");
        lblSelPages.setText(fullBook.getPageCount() != null ? fullBook.getPageCount().toString() : "-");
        lblSelGenres.setText(valueOrDash(fullBook.getGenres()));
        lblSelIsbn.setText(valueOrDash(fullBook.getIsbn()));
        lblSelBbk.setText(valueOrDash(fullBook.getBbk()));
        lblSelAuthorMark.setText(valueOrDash(fullBook.getAuthorMark()));

        if (fullBook.getCoverImage() != null) {
            bookSelectCover.setImage(new Image(new ByteArrayInputStream(fullBook.getCoverImage())));
        } else {
            bookSelectCover.setImage(null);
        }
    }

    private Book enrichBook(Book catalogBook) {
        return bookDao.findByIsbn(catalogBook.getIsbn())
                .map(fullBook -> mergeBookData(catalogBook, fullBook))
                .orElse(catalogBook);
    }

    private Book mergeBookData(Book catalogBook, Book fullBook) {
        fullBook.setAuthors(firstNonBlank(fullBook.getAuthors(), catalogBook.getAuthors()));
        fullBook.setGenres(firstNonBlank(fullBook.getGenres(), catalogBook.getGenres()));
        fullBook.setPublisherName(firstNonBlank(fullBook.getPublisherName(), catalogBook.getPublisherName()));
        fullBook.setTotalCopies(firstNonNull(fullBook.getTotalCopies(), catalogBook.getTotalCopies()));
        fullBook.setAvailableCopies(firstNonNull(fullBook.getAvailableCopies(), catalogBook.getAvailableCopies()));
        return fullBook;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private String valueOrDash(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString();
        return text.isBlank() ? "-" : text;
    }

    @FXML
    private void onSelectAction() {
        if (bookOnlyMode) {
            Book selected = bookSelectionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                this.selectedBook = enrichBook(selected);
                this.resultInventoryNumber = null;
                closeWindow();
            }
            return;
        }
        Copy selected = copySelectionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            this.resultInventoryNumber = selected.getInventoryNumber();
            this.selectedBook = enrichBook(bookSelectionTable.getSelectionModel().getSelectedItem());
            closeWindow();
        }
    }

    @FXML
    private void onCancelClick() { this.resultInventoryNumber = null; this.selectedBook = null; closeWindow(); }
    private void closeWindow() { ((Stage) btnFinalSelect.getScene().getWindow()).close(); }
    public Integer getSelectedInventoryNumber() { return resultInventoryNumber; }
    public Book getSelectedBook() { return selectedBook; }
}
