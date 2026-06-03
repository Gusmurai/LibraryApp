package ru.library.libraryapp.domains;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WriteOff {
    private Integer writeOffId;
    private Integer inventoryNumber;
    private Integer tabelNumber;
    private Integer reasonId;
    private LocalDate writeOffDate;
    private String bookTitle;
    private String isbn;
    private String reason;
    private String librarianName;
    private BigDecimal cost;

    public Integer getWriteOffId() {
        return writeOffId;
    }

    public void setWriteOffId(Integer writeOffId) {
        this.writeOffId = writeOffId;
    }

    public Integer getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(Integer inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public Integer getTabelNumber() {
        return tabelNumber;
    }

    public void setTabelNumber(Integer tabelNumber) {
        this.tabelNumber = tabelNumber;
    }

    public Integer getReasonId() {
        return reasonId;
    }

    public void setReasonId(Integer reasonId) {
        this.reasonId = reasonId;
    }

    public LocalDate getWriteOffDate() {
        return writeOffDate;
    }

    public void setWriteOffDate(LocalDate writeOffDate) {
        this.writeOffDate = writeOffDate;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLibrarianName() {
        return librarianName;
    }

    public void setLibrarianName(String librarianName) {
        this.librarianName = librarianName;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public WriteOff(Integer inventoryNumber, Integer tabelNumber, Integer reasonId) {
        this.inventoryNumber = inventoryNumber;
        this.tabelNumber = tabelNumber;
        this.reasonId = reasonId;
        // Дату можно не передавать, так как в БД стоит DEFAULT CURRENT_DATE,
        // либо можно добавить: this.writeOffDate = java.time.LocalDate.now();
    }

    // Не забудь оставить и пустой конструктор, он нужен для работы JDBC
    public WriteOff() {}
}
