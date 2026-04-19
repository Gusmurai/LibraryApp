package ru.library.libraryapp.domains;

import java.time.LocalDateTime;

public class AuditLog {
    private Integer logId;
    private String tableName;
    private String operationType;
    private LocalDateTime operationTime;
    private String dbUser;
    private String oldData; // Здесь можно использовать String для хранения JSON
    private String newData;
}
