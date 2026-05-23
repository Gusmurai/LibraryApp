package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.GenreDao;
import ru.library.libraryapp.domains.Genre;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreDaoImpl implements GenreDao {
    @Override
    public List<Genre> findAll() {
        List<Genre> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM genres ORDER BY name")) {
            while (rs.next()) {
                Genre g = new Genre();
                g.setGenreId(rs.getInt("genre_id"));
                g.setName(rs.getString("name"));
                g.setDescription(rs.getString("description"));
                list.add(g);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Genre g = new Genre();
                g.setGenreId(rs.getInt("genre_id"));
                g.setName(rs.getString("name"));
                return Optional.of(g);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
}