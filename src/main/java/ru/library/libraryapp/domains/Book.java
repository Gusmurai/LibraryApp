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

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getBbk() {
        return bbk;
    }

    public String getAuthorMark() {
        return authorMark;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setBbk(String bbk) {
        this.bbk = bbk;
    }

    public void setAuthorMark(String authorMark) {
        this.authorMark = authorMark;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
