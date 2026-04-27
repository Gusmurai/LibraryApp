package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Copy;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для управления физическими экземплярами книг.
 */
public interface CopyDao {
    /**
     * Получает список всех экземпляров конкретного издания.
     * @param isbn международный номер книги
     * @return список объектов {@link Copy}
     */
    List<Copy> findByIsbn(String isbn);

    /**
     * Ищет конкретный экземпляр по его инвентарному номеру.
     * @param invNumber уникальный инвентарный номер
     * @return Optional с найденным экземпляром
     */
    Optional<Copy> findByInventoryNumber(Integer invNumber);

    /**
     * Оформляет поставку новых книг. Вызывает хранимую процедуру в БД для массового создания экземпляров.
     * @param isbn номер издания
     * @param supplierId идентификатор поставщика
     * @param price стоимость за единицу
     * @param quantity количество новых экземпляров
     */
    void addDelivery(String isbn, Integer supplierId, double price, int quantity);

    /**
     * Оформляет списание экземпляра. Вызывает хранимую процедуру списания в БД.
     * @param invNumber номер экземпляра
     * @param librarianId ID сотрудника, производящего списание
     * @param reasonId ID причины списания
     */
    void writeOff(Integer invNumber, Integer librarianId, Integer reasonId);

    /**
     * Поиск доступных для выдачи экземпляров конкретной книги по инвентарному номеру.
     * Используется в окне подбора книг для выдачи.
     * @param isbn номер издания
     * @param invNumberQuery часть инвентарного номера
     * @return список найденных свободных экземпляров
     */
    List<Copy> searchAvailableByInv(String isbn, String invNumberQuery);
}