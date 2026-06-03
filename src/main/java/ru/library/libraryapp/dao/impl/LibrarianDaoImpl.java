package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.LibrarianDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Librarian;
import java.sql.*;
import java.util.Optional;
/**
 * Реализация слоя доступа к данным для работы с таблицами и процедурами БД.
 */

@Slf4j
public class LibrarianDaoImpl implements LibrarianDao {
    @Override
    public Optional<Librarian> findByLogin(String dbLogin) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("librarian.findFullByLogin"))) {
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
        } catch (SQLException e) {
            log.error("Не удалось найти сотрудника по логину БД {}.", dbLogin, e);
        }
        return Optional.empty();
    }
}
