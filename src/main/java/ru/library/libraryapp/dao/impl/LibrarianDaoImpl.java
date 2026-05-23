package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.LibrarianDao;
import ru.library.libraryapp.domains.Librarian;
import java.sql.*;
import java.util.Optional;

public class LibrarianDaoImpl implements LibrarianDao {
    @Override
    public Optional<Librarian> findByLogin(String dbLogin) {
        String sql = "SELECT * FROM librarians WHERE db_login = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dbLogin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Librarian l = new Librarian();
                l.setTabelNumber(rs.getInt("tabel_number"));
                l.setDbLogin(rs.getString("db_login"));
                l.setLastName(rs.getString("last_name"));
                l.setFirstName(rs.getString("first_name"));
                l.setPatronymic(rs.getString("patronymic"));
                l.setActive(rs.getBoolean("is_active"));
                return Optional.of(l);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
}