package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Reader;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс слоя доступа к данным (DAO) для работы с читателями.
 */
public interface ReaderDao {

    /**
     * Регистрирует нового читателя (Скриншот "Новый читатель").
     * @param reader объект читателя с заполненными полями
     */
    void add(Reader reader);

    /**
     * Обновляет данные существующего читателя (Скриншот "Карточка читателя").
     * @param reader объект читателя с измененными данными
     */
    void update(Reader reader);

    /**
     * Ищет читателя по точному номеру билета.
     * @param ticketNumber уникальный номер билета
     * @return Optional с найденным читателем
     */
    Optional<Reader> findByTicketNumber(Integer ticketNumber);

    /**
     * Возвращает список всех читателей для отображения в главной таблице.
     * @return список читателей
     */
    List<Reader> findAll();

    /**
     * Универсальный поиск читателей для строки поиска в интерфейсе.
     * Ищет совпадения по номеру билета, Фамилии, Имени или номеру телефона.
     *
     * @param searchQuery текст из строки поиска
     * @return отфильтрованный список читателей
     */
    List<Reader> searchReaders(String searchQuery);

    /**
     * Меняет статус читателя (Архивация / Разархивация).
     * Соответствует кнопкам "В архив" и "Восстановить".
     *
     * @param ticketNumber номер билета читателя
     * @param isActive true - восстановить (активен), false - отправить в архив
     */
    void changeStatus(Integer ticketNumber, boolean isActive);
}
