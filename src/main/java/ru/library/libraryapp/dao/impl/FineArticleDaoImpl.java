package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.FineArticleDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.FineArticle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * Реализация слоя доступа к данным для работы с таблицами и процедурами БД.
 */

@Slf4j
public class FineArticleDaoImpl implements FineArticleDao {
    @Override
    public List<FineArticle> findAll() {
        List<FineArticle> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("fineArticle.findAll"))) {
            while (rs.next()) {
                list.add(mapFineArticle(rs));
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить статьи штрафов.", e);
        }
        return list;
    }

    @Override
    public Optional<FineArticle> findById(Integer id) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fineArticle.findById"))) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapFineArticle(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить статью штрафа {}.", id, e);
        }
        return Optional.empty();
    }

    private FineArticle mapFineArticle(ResultSet rs) throws SQLException {
        FineArticle fineArticle = new FineArticle();
        fineArticle.setArticleId(rs.getInt("article_id"));
        fineArticle.setName(rs.getString("name"));
        fineArticle.setBaseAmount(rs.getBigDecimal("base_amount"));
        return fineArticle;
    }
}
