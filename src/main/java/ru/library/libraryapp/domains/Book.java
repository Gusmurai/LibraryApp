package ru.library.libraryapp.domains;

import java.time.LocalDateTime;

public class Book {
    private String isbn;
    private String title;
    private Integer publicationYear;
    private Integer pageCount;
    private String bbk;
    private String authorMark;
    private byte[] coverImage;
    private Integer publisherId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
