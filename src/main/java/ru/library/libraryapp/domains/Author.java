package ru.library.libraryapp.domains;

import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Модель автора книги.
 */

public class Author {
    private Integer authorId;
    private String lastName;
    private String firstName;
    private String patronymic;
    private LocalDate birthDate;

    public Integer getAuthorId() {
        return authorId;
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

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
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
}
