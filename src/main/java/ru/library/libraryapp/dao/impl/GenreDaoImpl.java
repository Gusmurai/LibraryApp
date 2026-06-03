package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.GenreDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Genre;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GenreDaoImpl implements GenreDao {
    @Override
    public List<Genre> findAll() {
        List<Genre> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("genre.findAll"))) {
            while (rs.next()) {
                Genre g = new Genre();
                g.setGenreId(rs.getInt("genre_id"));
                g.setName(rs.getString("name"));
                g.setDescription(rs.getString("description"));
                list.add(g);
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить жанры.", e);
        }
        return list;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("genre.findById"))) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Genre g = new Genre();
                g.setGenreId(rs.getInt("genre_id"));
                g.setName(rs.getString("name"));
                return Optional.of(g);
            }
        } catch (SQLException e) {
            log.error("Не удалось найти жанр по id {}.", id, e);
        }
        return Optional.empty();
    }
}
