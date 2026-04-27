package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Genre;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы со справочником жанров.
 */
public interface GenreDao {
    List<Genre> findAll();
    Optional<Genre> findById(Integer id);
}