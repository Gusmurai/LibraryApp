package ru.library.libraryapp.domains;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fine {
    private Integer fineId;
    private Integer lendingId;
    private Integer articleId;
    private LocalDate issueDate;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private BigDecimal amount;
    private String comment;

    // === ДОБАВЛЕННЫЕ ПОЛЯ ДЛЯ ОТОБРАЖЕНИЯ ===
    private String articleName;  // Название статьи штрафа (из fine_articles)
    private String bookTitle;    // Название книги (из books)
    private Integer ticketNumber;
    private String readerFullName;

    // === ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ НОВЫХ ПОЛЕЙ ===
    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getReaderFullName() {
        return readerFullName;
    }

    public void setReaderFullName(String readerFullName) {
        this.readerFullName = readerFullName;
    }

    // === СУЩЕСТВУЮЩИЕ ГЕТТЕРЫ И СЕТТЕРЫ ===
    public Integer getFineId() {
        return fineId;
    }

    public void setFineId(Integer fineId) {
        this.fineId = fineId;
    }

    public Integer getLendingId() {
        return lendingId;
    }

    public void setLendingId(Integer lendingId) {
        this.lendingId = lendingId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
