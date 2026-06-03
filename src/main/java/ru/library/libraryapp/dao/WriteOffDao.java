package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.WriteOff;
import java.time.LocalDate;
import java.util.List;

/**
 * Интерфейс для работы с актами списания книг из фонда.
 */
public interface WriteOffDao {
    /**
     * Создание акта списания.
     * @param writeOff объект акта
     */
    void create(WriteOff writeOff);

    /**
     * Получение истории списаний по фильтрам.
     */
    List<WriteOff> findWithFilters(String query, LocalDate start, LocalDate end);

    /**
     * Удаление акта (восстановление книги в фонд).
     * @param inventoryNumber номер восстанавливаемого экземпляра
     */
    void restore(Integer inventoryNumber);
    /**
     * Создаёт акт списания экземпляра
     */
    void createWriteOff(Integer inventoryNumber, Integer librarianId, Integer reasonId, String comment);
}