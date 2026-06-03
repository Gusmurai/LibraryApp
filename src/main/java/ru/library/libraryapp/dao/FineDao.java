package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Fine;
import java.time.LocalDate;
import java.util.List;

/**
 * Интерфейс для работы с финансовыми взысканиями (штрафами).
 */
public interface FineDao {
    /**
     * Поиск штрафов по заданным критериям (используется во вкладке Штрафы).
     * @param query текст для поиска (билет №, ФИО читателя или название книги)
     * @param start начало периода
     * @param end конец периода
     * @param articleId фильтр по типу нарушения (null - если все)
     * @param status фильтр по оплате (null - все, true - оплачено, false - долг)
     * @return список отфильтрованных штрафов
     */
    List<Fine> findWithFilters(String query, LocalDate start, LocalDate end, Integer articleId, Boolean status);
    /** Штрафы конкретного читателя для вкладки Выдача */
    List<Fine> findUnpaidByReader(Integer ticketNumber);
    /**
     * Рассчитывает общую сумму штрафов в переданном списке (для вывода итога в UI).
     * @param currentList текущий отображаемый список штрафов
     * @return итоговая сумма в рублях
     */
    double calculateTotalSum(List<Fine> currentList);
    void payFine(Integer fineId);
    void updateNote(Integer fineId, String note);
    /**
     * Корректировка суммы штрафа (доступно для статьи "Порча книги").
     * @param fineId идентификатор записи
     * @param newAmount новая сумма взыскания
     */

    void updateAmount(Integer fineId, double newAmount);

    /**
     * Аннулирование штрафа за утерю при возврате книги или замене.
     * @param fineId идентификатор штрафа
     * @param mode 1 - Читатель нашел книгу (восстановление), 2 - Читатель принес замену
     */
    void annulFine(Integer fineId, int mode);
    /**
     * Создает новый штраф через хранимую процедуру sp_create_fine.
     */
    void createFine(Integer lendingId, Integer articleId, double amount, String comment, boolean isPaid);
    /**
     * Рассчитывает сумму штрафа через функцию БД
     * @param lendingId ID выдачи
     * @param articleId ID статьи штрафа
     * @param daysOverdue Количество дней просрочки (0 или null если не просрочка)
     * @return рассчитанная сумма штрафа
     */
    double calculateFineAmount(Integer lendingId, Integer articleId, Integer daysOverdue);
}