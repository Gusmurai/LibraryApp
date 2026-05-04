package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.AuthorDao;
import ru.library.libraryapp.domains.Author;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorDaoImpl implements AuthorDao {
    @Override
    public List<Author> findAll() {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT * FROM authors ORDER BY last_name";
        try (Connection conn = DbConnector.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) authors.add(mapAuthor(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return authors;
    }

    @Override
    public Optional<Author> findById(Integer id) {
        String sql = "SELECT * FROM authors WHERE author_id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapAuthor(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Author> findByLastName(String lastName) {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT * FROM authors WHERE last_name ILIKE ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + lastName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) authors.add(mapAuthor(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return authors;
    }

    private Author mapAuthor(ResultSet rs) throws SQLException {
        Author a = new Author();
        a.setAuthorId(rs.getInt("author_id"));
        a.setLastName(rs.getString("last_name"));
        a.setFirstName(rs.getString("first_name"));
        a.setPatronymic(rs.getString("patronymic"));
        if (rs.getDate("birth_date") != null)
            a.setBirthDate(rs.getDate("birth_date").toLocalDate());
        return a;
    }
}