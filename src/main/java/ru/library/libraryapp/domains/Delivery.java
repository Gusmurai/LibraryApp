package ru.library.libraryapp.domains;

import java.time.LocalDate;
/**
 * Модель записи о поставке экземпляров.
 */

public class Delivery {
    private Integer deliveryId;
    private String supplierInn;
    private Integer inventoryNumber;
    private LocalDate deliveryDate;

    public Integer getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Integer deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getSupplierInn() {
        return supplierInn;
    }

    public void setSupplierInn(String supplierInn) {
        this.supplierInn = supplierInn;
    }

    public Integer getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(Integer inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
