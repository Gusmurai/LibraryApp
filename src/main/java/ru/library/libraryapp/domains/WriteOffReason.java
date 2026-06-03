package ru.library.libraryapp.domains;

import java.time.LocalDateTime;
/**
 * Модель причины списания экземпляра.
 */

public class WriteOffReason {
    private Integer reasonId;
    private String name;

    public Integer getReasonId() {
        return reasonId;
    }

    public void setReasonId(Integer reasonId) {
        this.reasonId = reasonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
