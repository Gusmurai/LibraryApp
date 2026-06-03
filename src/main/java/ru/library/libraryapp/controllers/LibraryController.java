package ru.library.libraryapp.controllers;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.LibraryApplication;
import ru.library.libraryapp.dao.BookDao;
import ru.library.libraryapp.dao.FineDao;
import ru.library.libraryapp.dao.FineArticleDao;
import ru.library.libraryapp.dao.LendingDao;
import ru.library.libraryapp.dao.LibrarianDao;
import ru.library.libraryapp.dao.ReaderDao;
import ru.library.libraryapp.dao.ReportDao;
import ru.library.libraryapp.dao.ReservationDao;
import ru.library.libraryapp.dao.WriteOffDao;
import ru.library.libraryapp.dao.WriteOffReasonDao;
import ru.library.libraryapp.dao.impl.BookDaoImpl;
import ru.library.libraryapp.dao.impl.FineDaoImpl;
import ru.library.libraryapp.dao.impl.FineArticleDaoImpl;
import ru.library.libraryapp.dao.impl.LendingDaoImpl;
import ru.library.libraryapp.dao.impl.LibrarianDaoImpl;
import ru.library.libraryapp.dao.impl.ReaderDaoImpl;
import ru.library.libraryapp.dao.impl.ReportDaoImpl;
import ru.library.libraryapp.dao.impl.ReservationDaoImpl;
import ru.library.libraryapp.dao.impl.WriteOffDaoImpl;
import ru.library.libraryapp.dao.impl.WriteOffReasonDaoImpl;
import ru.library.libraryapp.domains.Book;
import ru.library.libraryapp.domains.Fine;
import ru.library.libraryapp.domains.FineArticle;
import ru.library.libraryapp.domains.Lending;
import ru.library.libraryapp.domains.Librarian;
import ru.library.libraryapp.domains.Reader;
import ru.library.libraryapp.domains.Reservation;
import ru.library.libraryapp.domains.WriteOff;
import ru.library.libraryapp.domains.WriteOffReason;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class LibraryController {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    @FXML private TextField searchField;
    @FXML private TableView<Reader> readersTable;
    @FXML private TableColumn<Reader, Integer> colTicket;
    @FXML private TableColumn<Reader, String> colLastName;
    @FXML private TableColumn<Reader, String> colFirstName;
    @FXML private TableColumn<Reader, String> colPatronymic;
    @FXML private TableColumn<Reader, String> colPhone;
    @FXML private TableColumn<Reader, String> colStatus;
    @FXML private Label lblLendingReaderStatus;
    @FXML private Button btnArchive, btnRestore, btnEditReader, btnAddReader, btnDetails, btnFormular;

    @FXML private Label lblMainStatus, lblCurrentUser;
    @FXML private ResourceBundle resources;
    @FXML private Button btnPayFine, btnEditFineNote, btnLogout;
    @FXML private TabPane mainTabPane;

    private final WriteOffDao writeOffDao = new WriteOffDaoImpl();
    private final ReaderDao readerDao = new ReaderDaoImpl();
    private final ObservableList<Reader> readerData = FXCollections.observableArrayList();

    @FXML private TextField searchReaderInLending;
    @FXML private TableView<Reader> searchReaderResultsTable;
    @FXML private TableColumn<Reader, Integer> colSearchTicket;
    @FXML private TableColumn<Reader, String> colSearchFullName;
    @FXML private TableColumn<Reader, String> colSearchDob;

    @FXML private ImageView readerLendingPhoto;
    @FXML private Label lblLendingReaderName, lblLendingReaderTicket, lblLendingReaderDob,
            lblLendingReaderPassport, lblLendingReaderPhone, lblLendingReaderAddress;

    @FXML private TableView<Fine> readerFinesTable;
    @FXML private TableColumn<Fine, String> colFineDate, colFineType, colFineSum, colFineNote;
    @FXML private TableView<Lending> onHandTable;
    @FXML private TableColumn<Lending, String> colHandTitle, colHandInv, colHandLendDate, colHandDueDate;

    @FXML private TextField inputInvNumber;
    @FXML private Button btnIssueBook, btnReturnBook, btnLostBook, btnRenewBook, btnFindBook;

    @FXML private TextField searchBookField;
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> colIsbn, colTitle, colAuthors, colGenres, colBookStatus;
    @FXML private TableColumn<Book, Integer> colTotal, colAvailable;
    @FXML private Button btnBookDetails, btnShowCopies, btnAddBook, btnEditBook, btnArchiveBook, btnRestoreBook;
    private final ObservableList<Book> bookData = FXCollections.observableArrayList();


    @FXML private TextField searchFineField;
    @FXML private DatePicker fineStartDate, fineEndDate;
    @FXML private ComboBox<String> fineStatusFilter, fineArticleFilter;
    @FXML private TableView<Fine> finesTable;
    @FXML private TableColumn<Fine, String> colGlobalFineIssued, colGlobalFinePaidDate, colGlobalFineReader,
            colGlobalFineBook, colGlobalFineType, colGlobalFineAmount, colGlobalFineStatus, colGlobalFineNote;
    @FXML private TableColumn<Fine, Integer> colGlobalFineTicket;
    @FXML private Label lblTotalFinesSum;
    @FXML private Button btnApplyFineFilters, btnChangeFineAmount, btnAnnulFine, btnAcceptPayment, btnEditFineNoteGlobal;
    private final ObservableList<Fine> fineData = FXCollections.observableArrayList();
    private List<FineArticle> fineArticles = List.of();


    @FXML private TextField searchWriteOffField;
    @FXML private DatePicker writeOffStartDate, writeOffEndDate;
    @FXML private ComboBox<String> writeOffReasonFilter;
    @FXML private TableView<WriteOff> writeOffsTable;
    @FXML private TableColumn<WriteOff, String> colWriteOffDate, colWriteOffTitle, colWriteOffReason, colWriteOffEmployee, colWriteOffPrice;
    @FXML private TableColumn<WriteOff, Integer> colWriteOffInv;
    @FXML private Label lblWriteOffCount, lblWriteOffTotalSum;
    @FXML private Button btnApplyWriteOffFilters, btnRestoreToFund;
    private final ObservableList<WriteOff> writeOffData = FXCollections.observableArrayList();
    private List<WriteOffReason> writeOffReasons = List.of();


    @FXML private TextField searchReservationField;
    @FXML private ComboBox<String> reservationStatusFilter;
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> colResDate, colResExpiry, colResReader, colResReaderPhone,
            colResBook, colResLibrarian, colResStatus;
    @FXML private TableColumn<Reservation, Integer> colResTicket;
    @FXML private Button btnApplyResFilters, btnNewReservation, btnIssueFromRes, btnCancelReservation;
    private final ObservableList<Reservation> reservationData = FXCollections.observableArrayList();


    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker reportStartDate, reportEndDate;
    @FXML private TableView<Map<String, Object>> reportResultTable;
    @FXML private Button btnGenerateReport, btnExportReport;
    private final ObservableList<Map<String, Object>> reportData = FXCollections.observableArrayList();
    private final List<String> reportKeys = List.of(
            "reader_activity",
            "debtors",
            "popular_books",
            "unpaid_fines",
            "fund_state",
            "write_offs",
            "deliveries"
    );

    // === DAO ===
    private final BookDao bookDao = new BookDaoImpl();
    private final LendingDao lendingDao = new LendingDaoImpl();
    private final FineDao fineDao = new FineDaoImpl();
    private final FineArticleDao fineArticleDao = new FineArticleDaoImpl();
    private final ReservationDao reservationDao = new ReservationDaoImpl();
    private final WriteOffReasonDao writeOffReasonDao = new WriteOffReasonDaoImpl();
    private final LibrarianDao librarianDao = new LibrarianDaoImpl();
    private final ReportDao reportDao = new ReportDaoImpl();

    private Reader selectedReaderLending;
    private Integer currentLibrarianId;

    @FXML
    public void initialize() {
        log.info("Инициализация главного окна библиотеки.");

        String user = DBHelper.getCurrentDbUser();
        if (user != null) {
            String headerText = resources.getString("header.employee").replace("-", user);
            lblCurrentUser.setText(headerText);
            Optional<Librarian> librarian = librarianDao.findByLogin(user);
            currentLibrarianId = librarian.map(Librarian::getTabelNumber).orElse(null);
            log.debug("Текущий пользователь БД: {}", user);
        }

        setupReadersTab();
        setupLendingTab();
        setupCatalogTab();
        setupFinesTab();
        setupWriteOffsTab();
        setupReservationsTab();
        setupReportsTab();
        loadReaders();
        loadBooks();
        loadGlobalFines();
        loadWriteOffs();
        loadReservations();
        btnLogout.setOnAction(e -> handleLogout());
    }


    private void setupReadersTab() {
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
                    setText(active ? resources.getString("column.status.active")
                            : resources.getString("column.status.archived"));
                }
            }
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            log.debug("Изменен поисковый запрос по читателям: {}", newVal);
            if (newVal == null || newVal.trim().isEmpty()) {
                loadReaders();
            } else {
                readerData.setAll(readerDao.searchReaders(newVal));
            }
        });

        readersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            boolean sel = (newSel != null);
            btnEditReader.setDisable(!sel);
            btnDetails.setDisable(!sel);
            btnFormular.setDisable(!sel);
            if (sel) {
                btnArchive.setDisable(!newSel.getActive());
                btnRestore.setDisable(newSel.getActive());
            } else {
                btnArchive.setDisable(true);
                btnRestore.setDisable(true);
            }
        });

        btnAddReader.setOnAction(e -> openReaderForm(null, false));
        btnEditReader.setOnAction(e -> openReaderForm(readersTable.getSelectionModel().getSelectedItem(), false));
        btnDetails.setOnAction(e -> openReaderForm(readersTable.getSelectionModel().getSelectedItem(), true));
        btnFormular.setOnAction(e -> openReaderFormular());
        btnArchive.setOnAction(e -> handleStatusChange(false));
        btnRestore.setOnAction(e -> handleStatusChange(true));
    }


    private void setupLendingTab() {

        colSearchTicket.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        colSearchFullName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        colSearchDob.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getBirthDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? dtf.format(d) : "-");
        });

        searchReaderResultsTable.getItems().setAll(readerDao.findAll());
        searchReaderInLending.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                searchReaderResultsTable.getItems().setAll(readerDao.findAll());
            } else {
                searchReaderResultsTable.getItems().setAll(readerDao.searchReaders(val));
            }
        });


        colFineDate.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getIssueDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? dtf.format(d) : "-");
        });
        colFineType.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getArticleName();
            return new javafx.beans.property.SimpleStringProperty(
                    name != null ? name : resources.getString("fine.article.unknown") + " #" + cellData.getValue().getArticleId());
        });
        colFineSum.setCellValueFactory(cellData -> {
            BigDecimal a = cellData.getValue().getAmount();
            return new javafx.beans.property.SimpleStringProperty(a != null ? a + " " + resources.getString("currency.rub") : "-");
        });
        colFineNote.setCellValueFactory(cellData -> {
            String c = cellData.getValue().getComment();
            return new javafx.beans.property.SimpleStringProperty(c != null ? c : "-");
        });

        // === Р В Р’В Р вЂ™Р’В Р В Р Р‹Р РЋРІР‚С”Р В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’В°Р В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’В±Р В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’В»Р В Р’В Р вЂ™Р’В Р В Р Р‹Р Р†Р вЂљР’ВР В Р’В Р В Р вЂ№Р В Р вЂ Р В РІР‚С™Р вЂ™Р’В Р В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’В° Р В Р’В Р вЂ™Р’В Р В Р Р‹Р Р†Р вЂљРЎСљР В Р’В Р вЂ™Р’В Р В Р’В Р Р†Р вЂљР’В¦Р В Р’В Р вЂ™Р’В Р В Р Р‹Р Р†Р вЂљР’ВР В Р’В Р вЂ™Р’В Р В Р Р‹Р Р†Р вЂљРІР‚Сљ Р В Р’В Р вЂ™Р’В Р В Р’В Р Р†Р вЂљР’В¦Р В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’В° Р В Р’В Р В Р вЂ№Р В Р’В Р Р†Р вЂљРЎв„ўР В Р’В Р В Р вЂ№Р В Р Р‹Р Р†Р вЂљРЎС™Р В Р’В Р вЂ™Р’В Р В Р Р‹Р Р†Р вЂљРЎСљР В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’В°Р В Р’В Р В Р вЂ№Р В Р вЂ Р В РІР‚С™Р вЂ™Р’В¦ ===
        colHandTitle.setCellValueFactory(cellData -> {
            String t = cellData.getValue().getBookTitle();
            return new javafx.beans.property.SimpleStringProperty(
                    t != null ? t : "Р В Р’В Р вЂ™Р’В Р В РІР‚в„ўР вЂ™Р’ВР В Р’В Р вЂ™Р’В Р В Р’В Р Р†Р вЂљР’В¦Р В Р’В Р вЂ™Р’В Р В Р’В Р Р†Р вЂљР’В . Р В Р’В Р В РІР‚В Р В Р вЂ Р В РІР‚С™Р РЋРІР‚С”Р В Р вЂ Р В РІР‚С™Р Р†Р вЂљРЎС™" + cellData.getValue().getInventoryNumber());
        });
        colHandInv.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        colHandLendDate.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getLendDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? dtf.format(d) : "-");
        });
        colHandDueDate.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getDueDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? dtf.format(d) : "-");
        });


        searchReaderResultsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, val) -> {
                    if (val != null) fillLendingReaderCard(val);
                });


        btnIssueBook.disableProperty()
                .bind(searchReaderResultsTable.getSelectionModel().selectedItemProperty().isNull());
        btnReturnBook.disableProperty()
                .bind(onHandTable.getSelectionModel().selectedItemProperty().isNull());
        btnRenewBook.disableProperty()
                .bind(onHandTable.getSelectionModel().selectedItemProperty().isNull());
        btnLostBook.disableProperty()
                .bind(onHandTable.getSelectionModel().selectedItemProperty().isNull());


        btnIssueBook.setOnAction(e -> handleIssue());
        btnReturnBook.setOnAction(e -> handleReturn());
        btnRenewBook.setOnAction(e -> handleRenew());
        btnLostBook.setOnAction(e -> handleLostBook());
        btnFindBook.setOnAction(e -> onFindBookClick());
        btnPayFine.setOnAction(e -> handlePayFine());
        btnEditFineNote.setOnAction(e -> handleEditFineNote());
    }

    private void setupCatalogTab() {
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthors.setCellValueFactory(new PropertyValueFactory<>("authors"));
        colGenres.setCellValueFactory(new PropertyValueFactory<>("genres"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        colBookStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                Boolean.TRUE.equals(cellData.getValue().getActive())
                        ? resources.getString("column.status.active")
                        : resources.getString("column.status.archived")));

        booksTable.setItems(bookData);
        searchBookField.textProperty().addListener((obs, old, val) -> loadBooks());
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, old, book) -> {
            boolean selected = book != null;
            btnBookDetails.setDisable(!selected);
            btnShowCopies.setDisable(!selected);
            btnEditBook.setDisable(!selected);
            btnArchiveBook.setDisable(!selected || !Boolean.TRUE.equals(book.getActive()));
            btnRestoreBook.setDisable(!selected || Boolean.TRUE.equals(book.getActive()));
        });

        btnBookDetails.setOnAction(e -> showBookDetails());
        btnShowCopies.setOnAction(e -> openCopiesView());
        btnArchiveBook.setOnAction(e -> changeBookStatus(false));
        btnRestoreBook.setOnAction(e -> changeBookStatus(true));
        btnAddBook.setOnAction(e -> openBookForm(null));
        btnEditBook.setOnAction(e -> openBookForm(booksTable.getSelectionModel().getSelectedItem()));
    }

    private void setupFinesTab() {
        fineArticles = fineArticleDao.findAll();
        fineStatusFilter.getItems().setAll(resources.getString("status.all"), resources.getString("fine.status.unpaidFilter"), resources.getString("fine.status.paidFilter"));
        fineStatusFilter.getSelectionModel().selectFirst();
        fineArticleFilter.getItems().setAll(resources.getString("status.all"));
        fineArticleFilter.getItems().addAll(fineArticles.stream().map(FineArticle::getName).toList());
        fineArticleFilter.getSelectionModel().selectFirst();

        colGlobalFineIssued.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatDate(cellData.getValue().getIssueDate())));
        colGlobalFinePaidDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatDate(cellData.getValue().getPaymentDate())));
        colGlobalFineTicket.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        colGlobalFineReader.setCellValueFactory(new PropertyValueFactory<>("readerFullName"));
        colGlobalFineBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colGlobalFineType.setCellValueFactory(new PropertyValueFactory<>("articleName"));
        colGlobalFineAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatMoneySafe(cellData.getValue().getAmount())));
        colGlobalFineStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                Boolean.TRUE.equals(cellData.getValue().getPaid()) ? resources.getString("fine.status.paid") : resources.getString("fine.status.unpaid")));
        colGlobalFineNote.setCellValueFactory(new PropertyValueFactory<>("comment"));
        finesTable.setItems(fineData);

        btnApplyFineFilters.setOnAction(e -> loadGlobalFines());
        searchFineField.setOnAction(e -> loadGlobalFines());
        btnAcceptPayment.setDisable(true);
        btnChangeFineAmount.setDisable(true);
        btnAnnulFine.setDisable(true);
        btnEditFineNoteGlobal.setDisable(true);
        finesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, fine) -> {
            boolean selected = fine != null;
            btnAcceptPayment.setDisable(!selected || Boolean.TRUE.equals(fine.getPaid()));
            btnChangeFineAmount.setDisable(!selected || Boolean.TRUE.equals(fine.getPaid()) || !isDamageFine(fine));
            btnAnnulFine.setDisable(!selected || Boolean.TRUE.equals(fine.getPaid()));
            btnEditFineNoteGlobal.setDisable(!selected);
        });

        btnAcceptPayment.setOnAction(e -> paySelectedGlobalFine());
        btnChangeFineAmount.setOnAction(e -> changeSelectedFineAmount());
        btnAnnulFine.setOnAction(e -> annulSelectedFine());
        btnEditFineNoteGlobal.setOnAction(e -> editSelectedGlobalFineNote());
    }

    private void setupWriteOffsTab() {
        writeOffReasons = writeOffReasonDao.findAll();
        writeOffReasonFilter.getItems().setAll(resources.getString("status.all"));
        writeOffReasonFilter.getItems().addAll(writeOffReasons.stream().map(WriteOffReason::getName).toList());
        writeOffReasonFilter.getSelectionModel().selectFirst();

        colWriteOffDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatDate(cellData.getValue().getWriteOffDate())));
        colWriteOffInv.setCellValueFactory(new PropertyValueFactory<>("inventoryNumber"));
        colWriteOffTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colWriteOffReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colWriteOffEmployee.setCellValueFactory(new PropertyValueFactory<>("librarianName"));
        colWriteOffPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatMoneySafe(cellData.getValue().getCost())));
        writeOffsTable.setItems(writeOffData);

        btnApplyWriteOffFilters.setOnAction(e -> loadWriteOffs());
        searchWriteOffField.setOnAction(e -> loadWriteOffs());
        btnRestoreToFund.disableProperty().bind(writeOffsTable.getSelectionModel().selectedItemProperty().isNull());
        btnRestoreToFund.setOnAction(e -> restoreSelectedWriteOff());
    }

    private void setupReservationsTab() {
        reservationStatusFilter.getItems().setAll(resources.getString("status.all"), resources.getString("reservation.status.active"), resources.getString("reservation.status.closed"), resources.getString("reservation.status.overdue"));

        colResDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatDate(cellData.getValue().getReservationDate())));
        colResExpiry.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatDate(cellData.getValue().getExpiryDate())));
        colResTicket.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        colResReader.setCellValueFactory(new PropertyValueFactory<>("readerName"));
        colResReaderPhone.setCellValueFactory(new PropertyValueFactory<>("readerPhone"));
        colResBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colResLibrarian.setCellValueFactory(new PropertyValueFactory<>("librarianName"));
        colResStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        reservationsTable.setItems(reservationData);

        btnApplyResFilters.setOnAction(e -> loadReservations());
        searchReservationField.setOnAction(e -> loadReservations());
        btnNewReservation.setOnAction(e -> createReservationFromDialog());
        btnCancelReservation.disableProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> !isReservationActionAllowed(reservationsTable.getSelectionModel().getSelectedItem()),
                        reservationsTable.getSelectionModel().selectedItemProperty()));
        btnIssueFromRes.disableProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> !isReservationActionAllowed(reservationsTable.getSelectionModel().getSelectedItem()),
                        reservationsTable.getSelectionModel().selectedItemProperty()));
        btnCancelReservation.setOnAction(e -> cancelSelectedReservation());
        btnIssueFromRes.setOnAction(e -> issueSelectedReservation());
    }

    private void setupReportsTab() {
        reportTypeCombo.getItems().setAll(reportKeys.stream()
                .map(this::reportTitle)
                .toList());
        reportTypeCombo.getSelectionModel().selectFirst();
        reportStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        reportEndDate.setValue(LocalDate.now());
        reportResultTable.setItems(reportData);
        reportResultTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        btnGenerateReport.setOnAction(e -> generateSelectedReport());
        btnExportReport.setOnAction(e -> exportCurrentReport());
        btnExportReport.setDisable(true);
        reportTypeCombo.valueProperty().addListener((obs, old, value) -> updateReportPeriodState());
        updateReportPeriodState();
    }

    private void updateReportPeriodState() {
        boolean periodRequired = selectedReportRequiresPeriod();
        reportStartDate.setDisable(!periodRequired);
        reportEndDate.setDisable(!periodRequired);
    }

    private boolean selectedReportRequiresPeriod() {
        String key = selectedReportKey();
        return !"debtors".equals(key) && !"fund_state".equals(key);
    }

    private void generateSelectedReport() {
        String key = selectedReportKey();
        if (key == null) return;
        LocalDate start = reportStartDate.getValue();
        LocalDate end = reportEndDate.getValue();
        if (selectedReportRequiresPeriod()) {
            if (start == null || end == null) {
                showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.reportPeriodRequired"));
                return;
            }
            if (end.isBefore(start)) {
                showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.reportPeriodInvalid"));
                return;
            }
        }

        try {
            List<Map<String, Object>> rows = reportDao.generateReport(key, start, end);
            reportData.setAll(rows);
            rebuildReportColumns(rows);
            btnExportReport.setDisable(rows.isEmpty());
            showSuccess(String.format(resources.getString("success.reportGenerated"), rows.size()));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void rebuildReportColumns(List<Map<String, Object>> rows) {
        reportResultTable.getColumns().clear();
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (String columnName : rows.get(0).keySet()) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(reportColumnTitle(columnName));
            column.setMinWidth(95);
            column.setPrefWidth(reportColumnWidth(columnName));
            column.setMaxWidth(reportColumnWidth(columnName) + 80);
            column.setCellValueFactory(cell -> {
                Object value = cell.getValue().get(columnName);
                return new javafx.beans.property.SimpleStringProperty(formatReportValue(columnName, value));
            });
            column.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setStyle(empty ? "" : reportColumnCellStyle(columnName));
                }
            });
            reportResultTable.getColumns().add(column);
        }
    }

    private String reportColumnTitle(String columnName) {
        String key = "report.column." + columnName;
        return resources.containsKey(key) ? resources.getString(key) : columnName;
    }

    private double reportColumnWidth(String columnName) {
        return switch (columnName) {
            case "ticket_number", "ticket", "inv_num", "isbn", "year", "copies_count", "books_taken", "popularity",
                 "days_overdue", "count" -> 115;
            case "phone" -> 135;
            case "issued_date", "due_date", "w_date", "d_date" -> 120;
            case "sum", "cost", "price", "total" -> 125;
            case "reader_name", "reader", "supplier", "publisher", "authors" -> 210;
            case "title", "book", "reason" -> 240;
            default -> 135;
        };
    }

    private String reportColumnCellStyle(String columnName) {
        if (isMoneyReportColumn(columnName)
                || columnName.equals("copies_count")
                || columnName.equals("books_taken")
                || columnName.equals("popularity")
                || columnName.equals("days_overdue")
                || columnName.equals("count")
                || columnName.equals("year")) {
            return "-fx-alignment: CENTER-RIGHT;";
        }
        if (columnName.contains("date") || columnName.equals("w_date") || columnName.equals("d_date")) {
            return "-fx-alignment: CENTER;";
        }
        return "-fx-alignment: CENTER-LEFT;";
    }

    private String formatReportValue(String columnName, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof java.sql.Date date) {
            return formatDate(date.toLocalDate());
        }
        if (value instanceof LocalDate date) {
            return formatDate(date);
        }
        if (value instanceof BigDecimal amount && isMoneyReportColumn(columnName)) {
            return formatMoneySafe(amount);
        }
        return value.toString();
    }

    private boolean isMoneyReportColumn(String columnName) {
        return columnName.equals("sum")
                || columnName.equals("cost")
                || columnName.equals("price")
                || columnName.equals("total");
    }

    private void exportCurrentReport() {
        if (reportData.isEmpty()) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.noReportData"));
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(resources.getString("window.title.exportReport"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName(selectedReportKey() + ".csv");
        Stage stage = (Stage) reportResultTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        try {
            reportDao.exportToExcel(reportData, file.getAbsolutePath());
            showSuccess(resources.getString("success.reportExported"));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private String selectedReportKey() {
        int index = reportTypeCombo.getSelectionModel().getSelectedIndex();
        return index >= 0 && index < reportKeys.size() ? reportKeys.get(index) : null;
    }

    private String reportTitle(String key) {
        return resources.getString("report." + key);
    }

    private boolean ensureLibrarianBound() {
        if (currentLibrarianId != null) {
            return true;
        }
        showErrorAlert(resources.getString("alert.error.title"),
                resources.getString("error.librarianBindingRequired"));
        return false;
    }

    private void fillLendingReaderCard(Reader r) {
        this.selectedReaderLending = r;
        lblLendingReaderName.setText(r.getFullName());
        lblLendingReaderTicket.setText(resources.getString("column.ticket") + ": " + r.getTicketNumber());
        lblLendingReaderPassport.setText(r.getPassportSeries() + " " + r.getPassportNumber());
        lblLendingReaderPhone.setText(r.getPhone());
        lblLendingReaderAddress.setText(r.getAddress());

        lblLendingReaderStatus.setText(r.getActive()
                ? resources.getString("status.active") : resources.getString("status.blocked"));
        lblLendingReaderStatus.setStyle(r.getActive()
                ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        if (r.getPhoto() != null) {
            readerLendingPhoto.setImage(new Image(new ByteArrayInputStream(r.getPhoto())));
        } else {
            readerLendingPhoto.setImage(null);
        }
        if (r.getBirthDate() != null) {
            lblLendingReaderDob.setText(dtf.format(r.getBirthDate()));
        } else {
            lblLendingReaderDob.setText("-");
        }
        refreshLendingData();
    }

    private void refreshLendingData() {
        if (selectedReaderLending != null) {
            readerFinesTable.getItems().setAll(
                    fineDao.findUnpaidByReader(selectedReaderLending.getTicketNumber()));
            onHandTable.getItems().setAll(
                    lendingDao.findActiveByReader(selectedReaderLending.getTicketNumber()));
        }
    }


    private void handleIssue() {
        if (!ensureLibrarianBound()) return;
        if (selectedReaderLending == null) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.noReaderSelected"));
            return;
        }
        String inv = inputInvNumber.getText().trim();
        if (inv.isEmpty()) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.noCopySelected"));
            return;
        }
        if (!selectedReaderLending.getActive()) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.readerInArchive"));
            return;
        }
        try {
            int invNum = Integer.parseInt(inv);
            if (!confirmAction(resources.getString("button.issue"), String.format(resources.getString("text.confirmIssueBook"), invNum))) {
                return;
            }
            log.info("Экземпляр {} выдан читателю {}.", invNum, selectedReaderLending.getTicketNumber());
            lendingDao.issueBook(selectedReaderLending.getTicketNumber(), invNum, currentLibrarianId);
            showSuccess(String.format(resources.getString("success.bookIssued"), invNum));
            inputInvNumber.clear();
            refreshLendingData();
        } catch (NumberFormatException e) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.invalidInvFormat"));
        } catch (Exception e) {
            log.error("Не удалось выдать книгу: {}", e.getMessage());
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }


    private void handleReturn() {
        if (!ensureLibrarianBound()) return;
        Lending sel = onHandTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            lendingDao.returnBook(sel.getLendingId(), currentLibrarianId);
            askDamageFineIfNeeded(sel, resources.getString("text.bookReturnedCondition"));
            showSuccess(resources.getString("success.bookReturned"));
            refreshLendingData();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("OVERDUE:")) {
                String[] parts = e.getMessage().split(":", 3);
                int days = Integer.parseInt(parts[1]);
                showInfo(String.format(resources.getString("text.overdueDetected"), days));

                if (!openFineForm(sel.getLendingId(), 1, resources.getString("fine.article.overdue"), null, false)) {
                    showInfo(resources.getString("success.returnCancelledByFineCancel"));
                    refreshLendingData();
                    return;
                }

                try {
                    lendingDao.closeReturn(sel.getLendingId());
                    askDamageFineIfNeeded(sel, resources.getString("text.bookReturnedOverdue"));
                    showSuccess(resources.getString("success.returnedWithOverdueFine"));
                    refreshLendingData();
                } catch (RuntimeException closeError) {
                    showErrorAlert(resources.getString("error.returnTitle"), extractUserFriendlyMessage(closeError.getMessage()));
                }
            } else {
                showErrorAlert(resources.getString("error.returnTitle"), extractUserFriendlyMessage(e.getMessage()));
            }
        }
    }

    private void askDamageFineIfNeeded(Lending lending, String headerPattern) {
        Alert cond = new Alert(Alert.AlertType.CONFIRMATION);
        attachOwner(cond);
        cond.setTitle(resources.getString("window.title.checkCondition"));
        cond.setHeaderText(headerPattern.contains("%s")
                ? String.format(headerPattern, lending.getBookTitle())
                : headerPattern);
        cond.setContentText(resources.getString("text.askDamage"));
        localizeAlertButtons(cond, resources.getString("button.createFineYes"), resources.getString("button.noAllGood"));

        Optional<ButtonType> res = cond.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            openFineForm(lending.getLendingId(), 2, resources.getString("fine.article.damage"), null, false);
        }
    }
    private void handleRenew() {
        Lending sel = onHandTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            lendingDao.renewBook(sel.getLendingId());
            showSuccess(resources.getString("success.bookRenewed"));
            refreshLendingData();
        } catch (Exception e) {
            log.error("Не удалось продлить выдачу: {}", e.getMessage());
            showErrorAlert(resources.getString("error.renewTitle"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    @FXML
    private void handleLostBook() {
        if (!ensureLibrarianBound()) return;
        Lending sel = onHandTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        if (!confirmAction(resources.getString("window.title.lostFine"), String.format(resources.getString("text.confirmLostBook"), sel.getBookTitle()))) {
            return;
        }

        if (sel.getDueDate() != null && sel.getDueDate().isBefore(LocalDate.now())) {
            long days = ChronoUnit.DAYS.between(sel.getDueDate(), LocalDate.now());
            showInfo(String.format(resources.getString("text.lostBookOverdue"), days));
            if (!openFineForm(sel.getLendingId(), 1, resources.getString("fine.article.overdue"), null, false)) {
                return;
            }
        }

        if (!openFineForm(sel.getLendingId(), 3, resources.getString("fine.article.lost"), null, false)) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/library/libraryapp/write-off-form.fxml"), resources);
            Parent root = loader.load();
            WriteOffFormController ctrl = loader.getController();
            ctrl.setLibrarianId(currentLibrarianId);
            ctrl.initWriteOffData(sel.getInventoryNumber(), sel.getBookTitle(), resources.getString("writeOff.reason.lostReader"), true);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("window.title.writeOff"));
            stage.setScene(new Scene(root));
            attachOwner(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (ctrl.isSaveSuccessful()) {
                showSuccess(resources.getString("success.lostWrittenOff"));
                refreshLendingData();
            }
        } catch (IOException e) {
            log.error("Ошибка формы списания.", e);
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.openWriteOffForm"));
        }
    }
    private void handlePayFine() {
        Fine sel = readerFinesTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        attachOwner(confirm);
        confirm.setTitle(resources.getString("alert.confirm.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(String.format(resources.getString("text.confirmPayFine"), sel.getAmount()));
        localizeAlertButtons(confirm, resources.getString("button.pay"), resources.getString("button.cancel"));

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                fineDao.payFine(sel.getFineId());
                showSuccess(resources.getString("success.finePaid"));
                refreshLendingData();
            } catch (Exception e) {
                showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
            }
        }
    }

    private void handleEditFineNote() {
        Fine sel = readerFinesTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        TextInputDialog dlg = new TextInputDialog(sel.getComment());
        attachOwner(dlg);
        dlg.setTitle(resources.getString("window.title.editNote"));
        dlg.setHeaderText(null);
        dlg.setContentText(resources.getString("label.enterNote"));
        localizeDialogButtons(dlg);

        Optional<String> res = dlg.showAndWait();
        res.ifPresent(note -> {
            try {
                fineDao.updateNote(sel.getFineId(), note);
                refreshLendingData();
                showSuccess(resources.getString("success.noteUpdated"));
            } catch (Exception e) {
                showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
            }
        });
    }


    private boolean openFineForm(Integer lendingId, Integer articleId, String articleName, Double amount, boolean isPaid) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/library/libraryapp/fine-form.fxml"), resources);
            Parent root = loader.load();
            FineFormController ctrl = loader.getController();
            ctrl.initFineData(lendingId, articleId, articleName, amount, isPaid);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("window.title.fine"));
            stage.setScene(new Scene(root));
            attachOwner(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (ctrl.isSaveSuccessful()) {
                refreshLendingData();
                loadGlobalFines();
            }
            return ctrl.isSaveSuccessful();
        } catch (IOException e) {
            log.error("Не удалось открыть форму штрафа.", e);
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.openFineForm"));
            return false;
        }
    }

    private boolean openFineForm(Integer lendingId, Integer articleId, String articleName, Double amount) {
        return openFineForm(lendingId, articleId, articleName, amount, false);
    }

    private Integer openLostFineAnnulDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/library/libraryapp/fine-annul-view.fxml"), resources);
        Parent root = loader.load();
        FineAnnulController controller = loader.getController();
        Stage stage = new Stage();
        stage.setTitle(resources.getString("window.title.annulAction"));
        stage.setScene(new Scene(root));
        attachOwner(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        return controller.getSelectedMode();
    }

        private boolean isDamageFine(Fine fine) {
        return fine != null && (Integer.valueOf(2).equals(fine.getArticleId())
                || containsAny(fine.getArticleName(), "\u043f\u043e\u0440\u0447", "\u043f\u043e\u0432\u0440\u0435\u0436\u0434", "damage", "besch\u00e4d", "beschad"));
    }

        private boolean isLostFine(Fine fine) {
        return fine != null && (Integer.valueOf(3).equals(fine.getArticleId())
                || containsAny(fine.getArticleName(), "\u0443\u0442\u0435\u0440", "\u0443\u0442\u0440\u0430\u0442", "\u043f\u043e\u0442\u0435\u0440", "lost", "loss", "verlust"));
    }

        private boolean isReaderLossReason(String reason) {
        return containsAny(reason, "\u0443\u0442\u0435\u0440", "\u0443\u0442\u0440\u0430\u0442", "\u043f\u043e\u0442\u0435\u0440", "loss", "lost", "verlust");
    }

    private boolean containsAny(String value, String... needles) {
        if (value == null) return false;
        String normalized = value.toLowerCase();
        for (String needle : needles) {
            if (normalized.contains(needle)) return true;
        }
        return false;
    }

    private boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        attachOwner(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        localizeAlertButtons(alert, resources.getString("button.ok"), resources.getString("button.cancel"));
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void handleLogout() {
        if (!confirmAction(resources.getString("button.exit"), resources.getString("text.confirmLogout"))) {
            return;
        }
        try {
            DBHelper.closeConnection();
            LibraryApplication.showLoginView();
            log.info("Пользователь вышел из системы.");
        } catch (Exception e) {
            log.error("Не удалось выполнить выход из системы.", e);
            showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void showInfo(String message) {
        showInfo(resources.getString("alert.info.title"), message);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        attachOwner(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void localizeAlertButtons(Alert alert, String okText, String cancelText) {
        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        Button cancel = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (ok != null) {
            ok.setText(okText);
        }
        if (cancel != null) {
            cancel.setText(cancelText);
        }
    }

    private void localizeDialogButtons(Dialog<?> dialog) {
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (ok != null) {
            ok.setText(resources.getString("button.ok"));
        }
        if (cancel != null) {
            cancel.setText(resources.getString("button.cancel"));
        }
    }

    private void attachOwner(Dialog<?> dialog) {
        Window owner = getMainWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

    private void attachOwner(Stage stage) {
        Window owner = getMainWindow();
        if (owner != null) {
            stage.initOwner(owner);
        }
    }

    private Window getMainWindow() {
        return mainTabPane != null && mainTabPane.getScene() != null
                ? mainTabPane.getScene().getWindow()
                : null;
    }

    private String extractUserFriendlyMessage(String msg) {
        String fallback = resources.getString("error.dbOther").trim();
        if (msg == null || msg.isBlank()) {
            return fallback;
        }
        String normalized = msg.toLowerCase();
        if (normalized.contains("\u043b\u0438\u043c\u0438\u0442 \u043f\u0440\u043e\u0434\u043b\u0435\u043d")
                || normalized.contains("\u043b\u0438\u043c\u0438\u0442 \u043f\u0440\u043e\u0434\u043b\u0435\u043d\u0438\u0439")) {
            return resources.getString("error.renewLimitExceeded");
        }
        if ((normalized.contains("\u043d\u0435\u043b\u044c\u0437\u044f \u043f\u0440\u043e\u0434\u043b\u0438\u0442\u044c")
                && normalized.contains("\u0441\u0440\u043e\u043a \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430"))
                || normalized.contains("\u0441\u0440\u043e\u043a \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430 \u0443\u0436\u0435 \u0438\u0441\u0442\u0435\u043a")
                || normalized.contains("\u0441\u0440\u043e\u043a \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430 \u0443\u0436\u0435 \u0438\u0441\u0442\u0451\u043a")) {
            return resources.getString("error.renewOverdue");
        }
        if (normalized.contains("лимит продлен") || normalized.contains("лимит продлений")
                || normalized.contains("renewal limit")) {
            return resources.getString("error.renewLimitExceeded");
        }
        if ((normalized.contains("нельзя продлить") && normalized.contains("срок возврата"))
                || normalized.contains("срок возврата уже истек")
                || normalized.contains("срок возврата уже истёк")
                || normalized.contains("already overdue")) {
            return resources.getString("error.renewOverdue");
        }
        String clean = msg
                .replaceAll("(?i)^ERROR:\\s*", "")
                .replaceAll("(?is)DETAIL:.*$", "")
                .replaceAll("(?is)HINT:.*$", "")
                .replaceAll("(?is)WHERE:.*$", "")
                .replaceAll("(?is)Где:.*$", "")
                .trim();
        if (clean.length() > 300) {
            clean = clean.substring(0, 300) + "...";
        }
        return clean.isEmpty() ? fallback : clean;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        attachOwner(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);

        String clean = extractUserFriendlyMessage(message);
        if (!clean.isEmpty()) clean = clean.substring(0,1).toUpperCase() + clean.substring(1);

        alert.setContentText(clean);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Node content = alert.getDialogPane().lookup(".content.label");
        if (content instanceof Label) ((Label) content).setWrapText(true);
        alert.getDialogPane().setPrefWidth(450);
        alert.showAndWait();
    }


    private void showSuccess(String msg) {
        lblMainStatus.setText(msg);
        new Thread(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> lblMainStatus.setText(""));
        }).start();
    }

    private void loadBooks() {
        String query = searchBookField.getText();
        bookData.setAll(query == null || query.isBlank()
                ? bookDao.findAll()
                : bookDao.searchBooks(query.trim()));
    }

    private void showBookDetails() {
        Book catalogBook = booksTable.getSelectionModel().getSelectedItem();
        if (catalogBook == null) return;
        Book book = bookDao.findByIsbn(catalogBook.getIsbn())
                .map(fullBook -> mergeBookDetails(catalogBook, fullBook))
                .orElse(catalogBook);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        attachOwner(alert);
        alert.setTitle(resources.getString("button.details"));
        alert.setHeaderText(book.getTitle());

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);

        javafx.scene.layout.VBox coverBox = new javafx.scene.layout.VBox(6);
        coverBox.setMinWidth(160);
        coverBox.setPrefWidth(160);
        coverBox.setStyle("-fx-alignment: top-center;");
        ImageView cover = new ImageView();
        cover.setFitHeight(220);
        cover.setFitWidth(150);
        cover.setPreserveRatio(true);
        if (book.getCoverImage() != null) {
            cover.setImage(new Image(new ByteArrayInputStream(book.getCoverImage())));
            coverBox.getChildren().add(cover);
        } else {
            Label noCover = new Label("-");
            noCover.setMinSize(150, 220);
            noCover.setStyle("-fx-alignment: center; -fx-border-color: #d0d7de; -fx-background-color: #f6f8fa;");
            coverBox.getChildren().add(noCover);
        }
        grid.add(coverBox, 0, 0);
        GridPane.setRowSpan(coverBox, 12);

        int row = 0;
        addBookDetailRow(grid, row++, "ISBN", book.getIsbn());
        addBookDetailRow(grid, row++, resources.getString("column.title"), book.getTitle());
        addBookDetailRow(grid, row++, resources.getString("column.authors"), book.getAuthors());
        addBookDetailRow(grid, row++, resources.getString("column.genres"), book.getGenres());
        addBookDetailRow(grid, row++, cleanLabel("label.publisher"), book.getPublisherName());
        addBookDetailRow(grid, row++, cleanLabel("label.publicationYear"), book.getPublicationYear());
        addBookDetailRow(grid, row++, cleanLabel("label.pageCount"), book.getPageCount());
        addBookDetailRow(grid, row++, cleanLabel("label.bbk"), book.getBbk());
        addBookDetailRow(grid, row++, cleanLabel("label.authorMark"), book.getAuthorMark());
        addBookDetailRow(grid, row++, resources.getString("column.total"), book.getTotalCopies());
        addBookDetailRow(grid, row++, resources.getString("column.available"), book.getAvailableCopies());
        addBookDetailRow(grid, row, resources.getString("column.status"),
                Boolean.TRUE.equals(book.getActive())
                        ? resources.getString("column.status.active")
                        : resources.getString("column.status.archived"));

        alert.getDialogPane().setContent(grid);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(560);
        alert.showAndWait();
    }

    private void openCopiesView() {
        Book book = booksTable.getSelectionModel().getSelectedItem();
        if (book == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/library/libraryapp/copies-view.fxml"), resources);
            Parent root = loader.load();
            CopiesController controller = loader.getController();
            controller.setBook(book);
            controller.setLibrarianId(currentLibrarianId);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("button.showCopies"));
            stage.setScene(new Scene(root));
            attachOwner(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isChanged()) {
                loadBooks();
                loadWriteOffs();
                refreshLendingData();
            }
        } catch (IOException e) {
            log.error("Не удалось открыть окно экземпляров.", e);
            showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void changeBookStatus(boolean toActive) {
        Book book = booksTable.getSelectionModel().getSelectedItem();
        if (book == null) return;
        try {
            bookDao.changeStatus(book.getIsbn(), toActive);
            loadBooks();
            showSuccess(resources.getString("success.statusChanged"));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void openReaderFormular() {
        Reader reader = readersTable.getSelectionModel().getSelectedItem();
        if (reader == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/library/libraryapp/formular-view.fxml"), resources);
            Parent root = loader.load();
            FormularController controller = loader.getController();
            controller.setReader(reader);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("window.title.readerFormular"));
            stage.setScene(new Scene(root));
            attachOwner(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            log.error("Не удалось открыть формуляр читателя.", e);
            showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void openBookForm(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/library/libraryapp/book-form.fxml"), resources);
            Parent root = loader.load();
            BookFormController controller = loader.getController();
            controller.setBook(book);

            Stage stage = new Stage();
            stage.setTitle(book == null
                    ? resources.getString("window.title.addBook")
                    : resources.getString("window.title.editBook"));
            stage.setScene(new Scene(root));
            attachOwner(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isSaveSuccessful()) {
                loadBooks();
                showSuccess(book == null ? resources.getString("success.bookAdded") : resources.getString("success.bookUpdated"));
            }
        } catch (IOException e) {
            log.error("Не удалось открыть форму книги.", e);
            showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void loadGlobalFines() {
        fineData.setAll(fineDao.findWithFilters(
                searchFineField.getText(),
                fineStartDate.getValue(),
                fineEndDate.getValue(),
                selectedArticleId(),
                selectedFinePaidStatus()
        ));
        lblTotalFinesSum.setText(formatMoneySafe(BigDecimal.valueOf(fineDao.calculateTotalSum(fineData))));
    }

    private Integer selectedArticleId() {
        String selected = fineArticleFilter.getSelectionModel().getSelectedItem();
        if (selected == null || resources.getString("status.all").equals(selected)) return null;
        return fineArticles.stream()
                .filter(article -> selected.equals(article.getName()))
                .map(FineArticle::getArticleId)
                .findFirst()
                .orElse(null);
    }

    private Boolean selectedFinePaidStatus() {
        String selected = fineStatusFilter.getSelectionModel().getSelectedItem();
        if (resources.getString("fine.status.paidFilter").equals(selected)) return true;
        if (resources.getString("fine.status.unpaidFilter").equals(selected)) return false;
        return null;
    }

    private void paySelectedGlobalFine() {
        Fine fine = finesTable.getSelectionModel().getSelectedItem();
        if (fine == null) return;
        if (!confirmAction(resources.getString("button.pay"),
                String.format(resources.getString("text.confirmPayFine"), fine.getAmount()))) {
            return;
        }
        try {
            fineDao.payFine(fine.getFineId());
            loadGlobalFines();
            refreshLendingData();
            showSuccess(resources.getString("success.finePaid"));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void changeSelectedFineAmount() {
        Fine fine = finesTable.getSelectionModel().getSelectedItem();
        if (fine == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/library/libraryapp/fine-amount-edit.fxml"),
                    resources
            );
            Parent root = loader.load();
            FineAmountEditController controller = loader.getController();
            controller.init(fine.getAmount());

            Stage stage = new Stage();
            stage.setTitle(resources.getString("window.title.changeFineAmount"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(finesTable.getScene().getWindow());
            stage.showAndWait();

            if (!controller.isSaved()) {
                return;
            }

            BigDecimal amount = controller.getNewAmount();
            if (!confirmAction(resources.getString("button.changeAmount"),
                    String.format(resources.getString("text.confirmChangeFineAmount"), fine.getFineId(), amount))) {
                return;
            }

            fineDao.updateAmount(fine.getFineId(), amount.doubleValue());
            loadGlobalFines();
            refreshLendingData();
            showSuccess(resources.getString("success.fineAmountUpdated"));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void annulSelectedFine() {
        Fine fine = finesTable.getSelectionModel().getSelectedItem();
        if (fine == null) return;
        if (Boolean.TRUE.equals(fine.getPaid())) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.annulPaidFine"));
            return;
        }
        try {
            if (isLostFine(fine)) {
                Integer mode = openLostFineAnnulDialog();
                if (mode == null) return;
                fineDao.annulFine(fine.getFineId(), mode);
                showInfo(mode == 1
                        ? resources.getString("success.lostFineAnnulFound")
                        : resources.getString("success.lostFineAnnulReplacement"));
            } else {
                if (!confirmAction(resources.getString("button.annul"),
                        String.format(resources.getString("text.confirmAnnulFine"), fine.getFineId()))) {
                    return;
                }
                fineDao.annulFine(fine.getFineId(), 2);
                showSuccess(resources.getString("success.fineAnnulled"));
            }
            loadGlobalFines();
            refreshLendingData();
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void editSelectedGlobalFineNote() {
        Fine fine = finesTable.getSelectionModel().getSelectedItem();
        if (fine == null) return;
        TextInputDialog dialog = new TextInputDialog(fine.getComment());
        attachOwner(dialog);
        dialog.setTitle(resources.getString("window.title.editNote"));
        dialog.setHeaderText(null);
        dialog.setContentText(resources.getString("label.enterNote"));
        localizeDialogButtons(dialog);
        dialog.showAndWait().ifPresent(note -> {
            try {
                fineDao.updateNote(fine.getFineId(), note);
                loadGlobalFines();
                refreshLendingData();
                showSuccess(resources.getString("success.noteUpdated"));
            } catch (Exception e) {
                showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
            }
        });
    }

    private void loadWriteOffs() {
        List<WriteOff> loaded = writeOffDao.findWithFilters(searchWriteOffField.getText(), writeOffStartDate.getValue(), writeOffEndDate.getValue());
        String reason = writeOffReasonFilter.getSelectionModel().getSelectedItem();
        if (reason != null && !resources.getString("status.all").equals(reason)) {
            loaded = loaded.stream().filter(writeOff -> reason.equals(writeOff.getReason())).toList();
        }
        writeOffData.setAll(loaded);
        lblWriteOffCount.setText(String.format(resources.getString("label.writeOffCountValue"), writeOffData.size()));
        BigDecimal total = writeOffData.stream()
                .map(WriteOff::getCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblWriteOffTotalSum.setText(resources.getString("column.sum") + ": " + formatMoneySafe(total));
    }

    private void restoreSelectedWriteOff() {
        WriteOff writeOff = writeOffsTable.getSelectionModel().getSelectedItem();
        if (writeOff == null) return;
        if (isReaderLossReason(writeOff.getReason()) || containsAny(writeOff.getReason(), "утрат", "утер")) {
            showInfo(resources.getString("alert.forbidden.title"),
                    resources.getString("text.restoreLostWriteOffForbidden"));
            return;
        }
        if (!confirmAction(resources.getString("button.restoreToFund"),
                String.format(resources.getString("text.confirmRestoreCopy"), writeOff.getInventoryNumber()))) {
            return;
        }
        try {
            writeOffDao.restore(writeOff.getInventoryNumber());
            loadWriteOffs();
            loadBooks();
            showSuccess(resources.getString("button.restoreToFund"));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void loadReservations() {
        reservationData.setAll(reservationDao.findWithFilters(
                searchReservationField.getText(),
                selectedReservationDbStatus()
        ));
    }

    private String selectedReservationDbStatus() {
        String selected = reservationStatusFilter.getSelectionModel().getSelectedItem();
        if (selected == null || resources.getString("status.all").equals(selected)) {
            return null;
        }
        if (resources.getString("reservation.status.active").equals(selected)) {
            return "\u0410\u043A\u0442\u0438\u0432\u043D\u0430";
        }
        if (resources.getString("reservation.status.closed").equals(selected)) {
            return "\u0417\u0430\u043A\u0440\u044B\u0442\u0430";
        }
        if (resources.getString("reservation.status.overdue").equals(selected)) {
            return "\u041F\u0440\u043E\u0441\u0440\u043E\u0447\u0435\u043D\u0430";
        }
        return selected;
    }

    private boolean isReservationActionAllowed(Reservation reservation) {
        return reservation != null
                && Boolean.TRUE.equals(reservation.getReservationStatus())
                && reservation.getExpiryDate() != null
                && !reservation.getExpiryDate().isBefore(LocalDate.now());
    }


    private void createReservationFromDialog() {
        if (!ensureLibrarianBound()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/library/libraryapp/reservation-form.fxml"), resources);
            Parent root = loader.load();
            ReservationFormController controller = loader.getController();
            controller.setLibrarianId(currentLibrarianId);
            Stage stage = new Stage();
            stage.setTitle(resources.getString("button.newReservation"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (controller.isSaveSuccessful()) {
                loadReservations();
                loadBooks();
                showSuccess(resources.getString("button.newReservation"));
            }
        } catch (IOException e) {
            log.error("Не удалось открыть форму бронирования.", e);
            showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void cancelSelectedReservation() {
        Reservation reservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (reservation == null) return;
        if (!isReservationActionAllowed(reservation)) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.reservationNotActive"));
            return;
        }
        if (!confirmAction(resources.getString("button.cancelReservation"),
                String.format(resources.getString("text.confirmCancelReservation"), reservation.getBookTitle()))) {
            return;
        }
        try {
            reservationDao.cancel(reservation.getReservationId());
            loadReservations();
            showSuccess(resources.getString("button.cancelReservation"));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private void issueSelectedReservation() {
        if (!ensureLibrarianBound()) return;
        Reservation reservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (reservation == null) return;
        if (!isReservationActionAllowed(reservation)) {
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.reservationNotActive"));
            return;
        }
        try {
            Reader reader = readerDao.findByTicketNumber(reservation.getTicketNumber()).orElse(null);
            if (reader == null) {
                showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.noReaderSelected"));
                return;
            }
            if (!Boolean.TRUE.equals(reader.getActive())) {
                showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.readerInArchive"));
                return;
            }

            fillLendingReaderCard(reader);
            mainTabPane.getSelectionModel().select(0);

            Integer inv = openBookSelectionForReservation(reservation.getIsbn());
            if (inv == null) {
                return;
            }
            if (!confirmAction(resources.getString("button.issueFromRes"),
                    String.format(resources.getString("text.confirmIssueReservation"), inv, reservation.getBookTitle(), reader.getFullName()))) {
                return;
            }
            lendingDao.issueBook(reservation.getTicketNumber(), inv, currentLibrarianId);
            reservationDao.cancel(reservation.getReservationId());
            inputInvNumber.clear();
            loadReservations();
            loadBooks();
            refreshLendingData();
            showSuccess(String.format(resources.getString("success.bookIssued"), inv));
        } catch (Exception e) {
            showErrorAlert(resources.getString("alert.error.title"), extractUserFriendlyMessage(e.getMessage()));
        }
    }

    private Integer openBookSelectionForReservation(String isbn) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/library/libraryapp/book-select-view.fxml"), resources);
        Parent root = loader.load();
        BookSelectController ctrl = loader.getController();
        ctrl.enableCopySelectionMode();
        ctrl.preselectBook(isbn);
        Stage stage = new Stage();
        stage.setTitle(resources.getString("window.title.bookSelect"));
        stage.setScene(new Scene(root));
        attachOwner(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        Integer inv = ctrl.getSelectedInventoryNumber();
        if (inv != null) {
            inputInvNumber.setText(inv.toString());
        }
        return inv;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : dtf.format(date);
    }

    private String formatMoneySafe(BigDecimal amount) {
        return amount == null ? "-" : amount + " " + resources.getString("currency.rub");
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "-" : amount + " " + resources.getString("currency.rub");
    }

    private String valueOrDash(Object value) {
        if (value == null) return "-";
        String text = value.toString();
        return text.isBlank() ? "-" : text;
    }

    private void addBookDetailRow(GridPane grid, int row, String labelText, Object value) {
        Label label = new Label(labelText + ":");
        label.setStyle("-fx-font-weight: bold;");
        Label valueLabel = new Label(valueOrDash(value));
        valueLabel.setWrapText(true);
        grid.add(label, 1, row);
        grid.add(valueLabel, 2, row);
    }

    private String cleanLabel(String resourceKey) {
        return resources.getString(resourceKey).replace("*", "");
    }

    private Book mergeBookDetails(Book catalogBook, Book fullBook) {
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


    private void loadReaders() {
        readerData.setAll(readerDao.findAll());
        readersTable.setItems(readerData);
    }

    private void handleStatusChange(boolean toActive) {
        Reader sel = readersTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String name = sel.getLastName() + " " + sel.getFirstName();
        String text = toActive ? resources.getString("alert.confirm.restore")
                : resources.getString("alert.confirm.archive");
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        attachOwner(a);
        a.setTitle(resources.getString("alert.confirm.title"));
        a.setHeaderText(null);
        a.setContentText(text + " " + name + "?");
        if (a.showAndWait().filter(r -> r == ButtonType.OK).isPresent()) {
            try {
                readerDao.changeStatus(sel.getTicketNumber(), toActive);
                loadReaders();
                showSuccess(resources.getString("success.statusChanged"));
            } catch (Exception e) {
                showErrorAlert(resources.getString("alert.error.title"), e.getMessage());
            }
        }
    }

    private void openReaderForm(Reader r, boolean viewOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/library/libraryapp/reader-form.fxml"), resources);
            Parent root = loader.load();
            ReaderFormController ctrl = loader.getController();
            ctrl.setReaderData(r, viewOnly);
            Stage s = new Stage();
            String key = viewOnly ? "window.title.detailsReader"
                    : (r == null ? "window.title.addReader" : "window.title.editReader");
            s.setTitle(resources.getString(key));
            s.setScene(new Scene(root));
            attachOwner(s);
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
            if (!viewOnly && ctrl.isSaveSuccessful()) {
                loadReaders();
                Reader saved = ctrl.getReader();
                showSuccess((r == null ? resources.getString("success.added")
                        : resources.getString("success.updated"))
                        + saved.getLastName() + " " + saved.getFirstName());
            }
        } catch (IOException e) {
            log.error("Не удалось открыть форму читателя.", e);
        }
    }

    private void onFindBookClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/library/libraryapp/book-select-view.fxml"), resources);
            Parent root = loader.load();
            BookSelectController ctrl = loader.getController();
            ctrl.enableCopySelectionMode();
            Stage s = new Stage();
            s.setTitle(resources.getString("window.title.bookSelect"));
            s.setScene(new Scene(root));
            attachOwner(s);
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
            Integer inv = ctrl.getSelectedInventoryNumber();
            if (inv != null) {
                inputInvNumber.setText(inv.toString());
                log.info("Из каталога выбран экземпляр: {}", inv);
            }
        } catch (IOException e) {
            log.error("Не удалось открыть окно выбора книги.", e);
            showErrorAlert(resources.getString("alert.error.title"), resources.getString("error.openBookSelect"));
        }
    }
}
