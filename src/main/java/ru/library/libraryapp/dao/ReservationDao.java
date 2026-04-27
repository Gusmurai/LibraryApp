package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Reservation;
import java.util.List;

/**
 * Интерфейс для управления процессами бронирования литературы.
 */
public interface ReservationDao {
    /**
     * Создает новую запись о бронировании издания для читателя.
     * @param isbn номер книги
     * @param ticketNumber номер билета читателя
     * @param librarianId ID библиотекаря, оформившего бронь
     */
    void add(String isbn, Integer ticketNumber, Integer librarianId);

    /**
     * Отменяет бронь (перевод в статус "Отменена").
     * @param reservationId ID записи
     */
    void cancel(Integer reservationId);

    /**
     * Поиск бронирований с фильтрацией.
     * @param query поиск по названию книги или ФИО читателя
     * @param status фильтр по статусу (Активна, Отменена, Выполнена)
     * @return список броней
     */
    List<Reservation> findWithFilters(String query, String status);

    /**
     * Проверяет наличие действующей брони у читателя на конкретную книгу.
     * @param ticketNumber номер читателя
     * @param isbn номер книги
     * @return true - если бронь существует и активна
     */
    boolean hasActiveReservation(Integer ticketNumber, String isbn);
}