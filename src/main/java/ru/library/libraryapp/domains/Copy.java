package ru.library.libraryapp.domains;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Модель физического экземпляра книги.
 */

public class Copy {
    private Integer inventoryNumber;
    private String isbn;
    private BigDecimal cost;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(Integer inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
