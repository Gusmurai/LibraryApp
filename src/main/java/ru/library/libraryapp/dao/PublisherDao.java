package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Publisher;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы со справочником издательств.
 */
public interface PublisherDao {
    List<Publisher> findAll();
    Optional<Publisher> findById(Integer id);
}