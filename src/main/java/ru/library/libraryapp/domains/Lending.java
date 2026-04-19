package ru.library.libraryapp.domains;

import java.time.LocalDate;

public class Lending {
    private Integer lendingId;
    private Integer inventoryNumber;
    private Integer ticketNumber;
    private Integer tabelNumber;
    private LocalDate lendDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Integer renewalsCount;
}
