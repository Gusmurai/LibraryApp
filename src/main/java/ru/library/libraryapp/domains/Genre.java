package ru.library.libraryapp.domains;

import java.time.LocalDateTime;

public class Genre {
    private Integer genreId;
    private String name;
    private String description;

    public Integer getGenreId() {
        return genreId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
