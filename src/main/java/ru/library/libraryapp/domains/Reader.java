package ru.library.libraryapp.domains;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reader {
    private Integer ticketNumber;
    private String lastName;
    private String firstName;
    private String patronymic;
    private LocalDate birthDate;
    private String passportSeries;
    private String passportNumber;
    private String address;
    private String phone;
    private LocalDate registrationDate;
    private byte[] photo;
    private Boolean isActive;
    public String getFullName() {
        String result = lastName + " " + firstName;
        if (patronymic != null && !patronymic.isEmpty()) {
            result += " " + patronymic;
        }
        return result;
    }
    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPassportSeries() {
        return passportSeries;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setPassportSeries(String passportSeries) {
        this.passportSeries = passportSeries;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
