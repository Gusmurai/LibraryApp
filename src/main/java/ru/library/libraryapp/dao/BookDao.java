package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Author;
import ru.library.libraryapp.domains.Book;
import ru.library.libraryapp.domains.Genre;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс слоя доступа к данным (DAO) для работы с библиографическим каталогом.
 * Обеспечивает управление данными об изданиях и их связями с авторами и жанрами.
 */
public interface BookDao {

    /**
     * Добавляет новую книгу (издание) в базу данных.
     * @param book объект книги с заполненными реквизитами
     */
    void add(Book book);

    /**
     * Обновляет библиографические данные существующей книги.
     * @param book объект книги с обновленной информацией
     */
    void update(Book book);

    /**
     * Выполняет поиск книги по её уникальному номеру ISBN.
     * @param isbn международный стандартный книжный номер
     * @return {@link Optional}, содержащий книгу, или пустой Optional, если ничего не найдено
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Возвращает полный список всех книг в библиотеке.
     * @return список объектов {@link Book}
     */
    List<Book> findAll();

    /**
     * Универсальный поиск книг по фрагменту текста.
     * Ищет совпадения по ISBN, названию, авторам или жанрам.
     * @param query поисковый запрос от пользователя
     * @return список найденных книг
     */
    List<Book> searchBooks(String query);

    /**
     * Изменяет статус активности книги в системе.
     * Используется для архивации или восстановления издания в каталоге.
     * @param isbn номер издания
     * @param isActive true - активна, false - в архиве
     */
    void changeStatus(String isbn, boolean isActive);

    /**
     * Получает текущее количество свободных (находящихся в библиотеке) экземпляров книги.
     * Используется для заполнения колонки "В наличии" в таблице каталога.
     * @param isbn номер издания
     * @return количество доступных штук
     */
    int getAvailableCount(String isbn);

    /**
     * Получает общее количество экземпляров книги в фонде (кроме списанных).
     * @param isbn номер издания
     * @return общее количество штук
     */
    int getTotalCount(String isbn);

    /**
     * Возвращает список авторов, привязанных к конкретной книге.
     * @param isbn номер издания
     * @return список объектов {@link Author}
     */
    List<Author> getAuthorsByIsbn(String isbn);

    /**
     * Возвращает список жанров, к которым относится данная книга.
     * @param isbn номер издания
     * @return список объектов {@link Genre}
     */
    List<Genre> getGenresByIsbn(String isbn);

    /**
     * Обновляет связи книги с авторами в таблице book_authors.
     * Метод актуализирует список создателей для конкретного издания.
     * @param isbn номер издания
     * @param authorIds список идентификаторов авторов
     */
    void updateAuthors(String isbn, List<Integer> authorIds);

    /**
     * Обновляет связи книги с жанрами в таблице book_genres.
     * Метод актуализирует перечень жанров для конкретного издания.
     * @param isbn номер издания
     * @param genreIds список идентификаторов жанров
     */
    void updateGenres(String isbn, List<Integer> genreIds);
}