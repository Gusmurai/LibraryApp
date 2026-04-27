package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Supplier;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы со справочником поставщиков.
 */
public interface SupplierDao {
    List<Supplier> findAll();
    Optional<Supplier> findByInn(String inn);
}