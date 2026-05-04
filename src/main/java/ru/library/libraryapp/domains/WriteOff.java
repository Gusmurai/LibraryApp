package ru.library.libraryapp.domains;

import java.time.LocalDate;

public class WriteOff {
    private Integer writeOffId;
    private Integer inventoryNumber;
    private Integer tabelNumber;
    private Integer reasonId;
    private LocalDate writeOffDate;

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
}
