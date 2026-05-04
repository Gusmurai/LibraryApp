package ru.library.libraryapp.domains;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Copy {
    private Integer inventoryNumber;
    private String isbn;
    private BigDecimal cost;

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
