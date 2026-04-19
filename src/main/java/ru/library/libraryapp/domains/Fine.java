package ru.library.libraryapp.domains;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fine {
    private Integer fineId;
    private Integer lendingId;
    private Integer articleId;
    private LocalDate issueDate;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private BigDecimal amount;
    private String comment;
}
