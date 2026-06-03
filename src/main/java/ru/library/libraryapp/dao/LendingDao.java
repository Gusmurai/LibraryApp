package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Lending;
import java.util.List;

/**
 * Интерфейс для операций выдачи, возврата и продления.
 */
public interface LendingDao {
    /** Оформление выдачи (кнопка "Выдать") */
    void issueBook(Integer ticketNumber, Integer inventoryNumber, Integer librarianId);

    /** Оформление возврата (кнопка "Возврат") */
    void returnBook(Integer lendingId, Integer librarianId);

    void closeReturn(Integer lendingId);

    /** Продление срока (кнопка "Продлить") */
    void renewBook(Integer lendingId);

    /** Фиксация утери (кнопка "Утеря книги") - инициирует штраф и списание */
    void markAsLost(Integer lendingId, Integer librarianId);

    /** Список книг на руках у конкретного читателя */
    List<Lending> findActiveByReader(Integer ticketNumber);

    /** История выдач конкретного экземпляра (для формуляра экземпляра) */
    List<Lending> findHistoryByInventoryNumber(Integer inventoryNumber);

    /** История выдач читателя (для формуляра читателя) */
    List<Lending> findHistoryByReader(Integer ticketNumber);
}
