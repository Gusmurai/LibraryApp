package ru.library.libraryapp.domains;

import java.time.LocalDate;

public class Reservation {
    private Integer reservationId;
    private String isbn;
    private Integer ticketNumber;
    private Integer tabelNumber;
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private String status;
}
