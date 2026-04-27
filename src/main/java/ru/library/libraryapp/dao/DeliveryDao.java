package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Delivery;
import java.time.LocalDate;
import java.util.List;

/**
 * Интерфейс для работы с документами (актами) поставки литературы.
 */
public interface DeliveryDao {

    /**
     * Регистрирует новую поставку книг в базу данных.
     * Метод должен вызывать транзакцию в БД: создавать запись о поставке
     * и генерировать новые физические экземпляры в таблице copies.
     *
     * @param isbn номер издания
     * @param supplierInn ИНН поставщика (может быть null)
     * @param price цена за единицу
     * @param quantity количество книг в партии
     */
    void registerDelivery(String isbn, String supplierInn, double price, int quantity);

    /**
     * Получает полную историю поставок (Журнал поставок).
     * @return список всех записей о поступлениях
     */
    List<Delivery> findAll();

    /**
     * Поиск поставок за определенный период или по конкретному поставщику.
     * @param start начало периода
     * @param end конец периода
     * @param supplierInn ИНН поставщика
     * @return отфильтрованный список поставок
     */
    List<Delivery> findWithFilters(LocalDate start, LocalDate end, String supplierInn);
}