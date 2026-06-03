package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.ReservationDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Reservation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReservationDaoImpl implements ReservationDao {

    @Override
    public void add(String isbn, Integer ticketNumber, Integer librarianId) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("reservation.create"))) {
            cs.setString(1, isbn);
            cs.setInt(2, ticketNumber);
            cs.setInt(3, librarianId);
            cs.execute();
            log.info("Бронирование создано. ISBN={}, читатель={}, сотрудник={}.", isbn, ticketNumber, librarianId);
        } catch (SQLException e) {
            log.error("Не удалось создать бронирование.", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void cancel(Integer reservationId) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("reservation.cancel"))) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
            log.info("Бронирование {} отменено.", reservationId);
        } catch (SQLException e) {
            log.error("Ошибка отмены бронирования {}.", reservationId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findWithFilters(String query, String status) {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SqlProvider.get("reservation.findWithFilters.base"));
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            appendSql(sql, "reservation.findWithFilters.query");
            String like = "%" + query.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(query.trim());
        }
        if (isSpecificStatus(status)) {
            appendSql(sql, "reservation.findWithFilters.status");
            params.add(status);
        }
        appendSql(sql, "reservation.findWithFilters.order");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка загрузки бронирования. SQL={}", sql, e);
        }
        return list;
    }

    @Override
    public boolean hasActiveReservation(Integer ticketNumber, String isbn) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("reservation.hasActive"))) {
            ps.setInt(1, ticketNumber);
            ps.setString(2, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Не удалось проверить активное бронирование.", e);
            return hasActiveReservationFallback(ticketNumber, isbn);
        }
    }

    private boolean hasActiveReservationFallback(Integer ticketNumber, String isbn) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("reservation.hasActiveNewSchema"))) {
            ps.setInt(1, ticketNumber);
            ps.setString(2, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException fallbackError) {
            log.error("Не удалось выполнить проверку активной брони.", fallbackError);
            return false;
        }
    }

    private boolean isSpecificStatus(String status) {
        return status != null
                && !status.isBlank()
                && !"Все".equalsIgnoreCase(status)
                && !"All".equalsIgnoreCase(status)
                && !"Alle".equalsIgnoreCase(status);
    }

    private void appendSql(StringBuilder sql, String key) {
        sql.append(' ').append(SqlProvider.get(key));
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setReservationId(rs.getInt("reservation_id"));
        res.setIsbn(rs.getString("isbn"));
        res.setTicketNumber(rs.getInt("ticket_number"));
        setIntegerIfPresent(rs, "tabel_number", res::setTabelNumber);
        res.setReservationDate(toLocalDate(rs.getDate("reservation_date")));
        res.setExpiryDate(toLocalDate(rs.getDate("expiry_date")));
        res.setReservationStatus(rs.getBoolean("reservation_status"));
        res.setStatus(rs.getString("status_text"));
        res.setBookTitle(rs.getString("book_title"));
        res.setReaderName(rs.getString("reader_name"));
        res.setReaderPhone(rs.getString("reader_phone"));
        setStringIfPresent(rs, "librarian_login", res::setLibrarianName);
        if (res.getLibrarianName() == null || res.getLibrarianName().isBlank()) {
            setStringIfPresent(rs, "librarian_name", res::setLibrarianName);
        }
        return res;
    }

    private java.time.LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private interface IntegerSetter {
        void set(Integer value);
    }

    private interface StringSetter {
        void set(String value);
    }

    private void setStringIfPresent(ResultSet rs, String column, StringSetter setter) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                setter.set(rs.getString(column));
                return;
            }
        }
    }

    private void setIntegerIfPresent(ResultSet rs, String column, IntegerSetter setter) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                int value = rs.getInt(column);
                if (!rs.wasNull()) {
                    setter.set(value);
                }
                return;
            }
        }
    }
}
