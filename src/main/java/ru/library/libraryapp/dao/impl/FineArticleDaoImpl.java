package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.FineArticleDao;
import ru.library.libraryapp.domains.FineArticle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FineArticleDaoImpl implements FineArticleDao {
    @Override
    public List<FineArticle> findAll() {
        List<FineArticle> list = new ArrayList<>();
        try (Connection conn = DbConnector.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM fine_articles")) {
            while (rs.next()) {
                FineArticle fa = new FineArticle();
                fa.setArticleId(rs.getInt("article_id"));
                fa.setName(rs.getString("name"));
                fa.setBaseAmount(rs.getBigDecimal("base_amount"));
                list.add(fa);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<FineArticle> findById(Integer id) {
        String sql = "SELECT * FROM fine_articles WHERE article_id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                FineArticle fa = new FineArticle();
                fa.setArticleId(rs.getInt("article_id"));
                fa.setName(rs.getString("name"));
                return Optional.of(fa);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
}