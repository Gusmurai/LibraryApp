package ru.library.libraryapp.domains;

import java.time.LocalDate;
/**
 * Модель записи о выдаче книги читателю.
 */

public class Lending {
    private Integer lendingId;
    private Integer inventoryNumber;
    private Integer ticketNumber;
    private Integer tabelNumber;
    private LocalDate lendDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Integer renewalsCount;


    private String bookTitle;
    private String readerFullName;
    private String readerPhone;
    private String librarianName;

    public String getBookTitle() {
        return bookTitle;
    }
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    public String getReaderFullName() {
        return readerFullName;
    }
    public void setReaderFullName(String readerFullName) {
        this.readerFullName = readerFullName;
    }
    public String getReaderPhone() {
        return readerPhone;
    }
    public void setReaderPhone(String readerPhone) {
        this.readerPhone = readerPhone;
    }
    public String getLibrarianName() {
        return librarianName;
    }
    public void setLibrarianName(String librarianName) {
        this.librarianName = librarianName;
    }
    public void setLendingId(Integer lendingId) {
        this.lendingId = lendingId;
    }

    public void setInventoryNumber(Integer inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public void setTabelNumber(Integer tabelNumber) {
        this.tabelNumber = tabelNumber;
    }

    public void setLendDate(LocalDate lendDate) {
        this.lendDate = lendDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setRenewalsCount(Integer renewalsCount) {
        this.renewalsCount = renewalsCount;
    }

    public Integer getLendingId() {
        return lendingId;
    }

    public Integer getInventoryNumber() {
        return inventoryNumber;
    }

    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public Integer getTabelNumber() {
        return tabelNumber;
    }

    public LocalDate getLendDate() {
        return lendDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public Integer getRenewalsCount() {
        return renewalsCount;
    }
}
