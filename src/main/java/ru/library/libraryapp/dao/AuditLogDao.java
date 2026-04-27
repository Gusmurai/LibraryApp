package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.AuditLog;
import java.util.List;

/**
 * Интерфейс для просмотра журнала аудита (только для чтения).
 */
public interface AuditLogDao {
    /**
     * Получение последних записей журнала.
     * @param limit количество записей
     * @return список событий
     */
    List<AuditLog> findRecent(int limit);
}