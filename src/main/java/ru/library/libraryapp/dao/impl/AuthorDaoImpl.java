package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.AuthorDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Author;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AuthorDaoImpl implements AuthorDao {
    @Override
    public List<Author> findAll() {
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("author.findAll"))) {
            while (rs.next()) authors.add(mapAuthor(rs));
        } catch (SQLException e) {
            log.error("Не удалось загрузить авторов.", e);
        }
        return authors;
    }

    @Override
    public Optional<Author> findById(Integer id) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("author.findById"))) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapAuthor(rs));
        } catch (SQLException e) {
            log.error("Не удалось найти автора по id {}.", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Author> findByLastName(String lastName) {
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("author.findByLastName"))) {
            ps.setString(1, "%" + lastName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) authors.add(mapAuthor(rs));
        } catch (SQLException e) {
            log.error("Не удалось найти авторов по фамилии {}.", lastName, e);
        }
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
