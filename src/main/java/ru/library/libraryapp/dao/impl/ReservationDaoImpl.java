package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.ReservationDao;
import ru.library.libraryapp.domains.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDaoImpl implements ReservationDao {

    @Override
    public void add(String isbn, Integer ticketNumber, Integer librarianId) {
        String sql = "CALL sp_create_reservation(?, ?, ?)";
        try (Connection conn = DbConnector.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, isbn);
            cs.setInt(2, ticketNumber);
            cs.setInt(3, librarianId);
            cs.execute();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }
    @Override
    public void cancel(Integer reservationId) {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Reservation> findWithFilters(String query, String status) {
        List<Reservation> list = new ArrayList<>();
        // JOIN-им таблицы для поиска по названиям и фамилиям
        StringBuilder sql = new StringBuilder(
                "SELECT res.* FROM reservations res " +
                        "JOIN books b ON res.isbn = b.isbn " +
                        "JOIN readers r ON res.ticket_number = r.ticket_number " +
                        "WHERE 1=1"
        );

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (b.title ILIKE ? OR r.last_name ILIKE ? OR CAST(r.ticket_number AS TEXT) = ?)");
        }

        if (status != null && !status.equals("Все")) {
            sql.append(" AND res.status = ?");
        }

        sql.append(" ORDER BY res.reservation_date DESC");

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (query != null && !query.isEmpty()) {
                String q = "%" + query + "%";
                ps.setString(paramIndex++, q);
                ps.setString(paramIndex++, q);
                ps.setString(paramIndex++, query);
            }

            if (status != null && !status.equals("Все")) {
                ps.setString(paramIndex, status);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean hasActiveReservation(Integer ticketNumber, String isbn) {
        String sql = "SELECT 1 FROM reservations WHERE ticket_number = ? AND isbn = ? " +
                "AND status = 'ACTIVE' AND expiry_date >= CURRENT_DATE";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketNumber);
            ps.setString(2, isbn);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setReservationId(rs.getInt("reservation_id"));
        res.setIsbn(rs.getString("isbn"));
        res.setTicketNumber(rs.getInt("ticket_number"));
        res.setTabelNumber(rs.getInt("tabel_number"));

        Date rDate = rs.getDate("reservation_date");
        if (rDate != null) res.setReservationDate(rDate.toLocalDate());

        Date eDate = rs.getDate("expiry_date");
        if (eDate != null) res.setExpiryDate(eDate.toLocalDate());

        res.setStatus(rs.getString("status"));
        return res;
    }
}