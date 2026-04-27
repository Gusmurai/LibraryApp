package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.WriteOffReason;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы со справочником причин списания книг.
 */
public interface WriteOffReasonDao {
    List<WriteOffReason> findAll();
    Optional<WriteOffReason> findById(Integer id);
}