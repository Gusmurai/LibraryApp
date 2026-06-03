package ru.library.libraryapp.domains;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Модель штрафа, начисленного читателю.
 */

public class Fine {
    private Integer fineId;
    private Integer lendingId;
    private Integer articleId;
    private LocalDate issueDate;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private BigDecimal amount;
    private String comment;

    // Эти поля нужны для вывода штрафов в таблицах приложения.
    private String articleName;
    private String bookTitle;
    private Integer ticketNumber;
    private String readerFullName;

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
