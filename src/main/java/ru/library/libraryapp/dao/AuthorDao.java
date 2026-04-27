package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Author;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с данными авторов книг.
 */
public interface AuthorDao {
    /**
     * Поиск автора по уникальному идентификатору.
     * @param id идентификатор автора
     * @return Optional с объектом автора
     */
    Optional<Author> findById(Integer id);

    /**
     * Получение всех авторов из справочника.
     * @return список авторов
     */
    List<Author> findAll();

    /**
     * Поиск авторов по части фамилии.
     * @param lastName фрагмент фамилии
     * @return список найденных авторов
     */
    List<Author> findByLastName(String lastName);
}