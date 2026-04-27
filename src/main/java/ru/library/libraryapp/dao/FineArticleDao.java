package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.FineArticle;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы со справочником статей штрафов (видов нарушений).
 */
public interface FineArticleDao {
    List<FineArticle> findAll();
    Optional<FineArticle> findById(Integer id);
}