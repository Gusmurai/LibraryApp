package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.FineDao;
import ru.library.libraryapp.domains.Fine;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FineDaoImpl implements FineDao {

    @Override
    public List<Fine> findUnpaidByReader(Integer ticketNumber) {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT f.* FROM fines f " +
                "JOIN lendings l ON f.lending_id = l.lending_id " +
                "WHERE l.ticket_number = ? AND f.is_paid = false";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFine(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Fine> findWithFilters(String query, LocalDate start, LocalDate end, Integer articleId, Boolean isPaid) {
        List<Fine> list = new ArrayList<>();
        // Строим динамический запрос
        StringBuilder sql = new StringBuilder(
                "SELECT f.* FROM fines f " +
                        "JOIN lendings l ON f.lending_id = l.lending_id " +
                        "JOIN readers r ON l.ticket_number = r.ticket_number " +
                        "JOIN copies c ON l.inventory_number = c.inventory_number " +
                        "JOIN books b ON c.isbn = b.isbn " +
                        "WHERE 1=1"
        );

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (r.last_name ILIKE ? OR b.title ILIKE ? OR CAST(r.ticket_number AS TEXT) = ?)");
        }
        if (start != null) sql.append(" AND f.issue_date >= ?");
        if (end != null) sql.append(" AND f.issue_date <= ?");
        if (articleId != null) sql.append(" AND f.article_id = ?");
        if (isPaid != null) sql.append(" AND f.is_paid = ?");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (query != null && !query.isEmpty()) {
                String q = "%" + query + "%";
                ps.setString(paramIndex++, q);
                ps.setString(paramIndex++, q);
                ps.setString(paramIndex++, query);
            }
            if (start != null) ps.setDate(paramIndex++, Date.valueOf(start));
            if (end != null) ps.setDate(paramIndex++, Date.valueOf(end));
            if (articleId != null) ps.setInt(paramIndex++, articleId);
            if (isPaid != null) ps.setBoolean(paramIndex++, isPaid);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFine(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public double calculateTotalSum(List<Fine> fines) {
        return fines.stream()
                .map(f -> f.getAmount().doubleValue())
                .reduce(0.0, Double::sum);
    }

    @Override
    public void payFine(Integer fineId) {
        String sql = "CALL sp_pay_fine(?)";
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, fineId);
            cs.execute();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }
    @Override
    public void updateNote(Integer fineId, String note) {
        String sql = "UPDATE fines SET comment = ? WHERE fine_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note);
            ps.setInt(2, fineId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void updateAmount(Integer fineId, double newAmount) {
        String sql = "CALL sp_update_fine_amount(?, ?)";
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, fineId);
            cs.setDouble(2, newAmount);
            cs.execute();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    @Override
    public void annulFine(Integer fineId, int mode) {
        String sql = "CALL sp_annul_fine(?, ?)";
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, fineId);
            cs.setBoolean(2, (mode == 1)); // true - нашел книгу, false - замена
            cs.execute();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    private Fine mapFine(ResultSet rs) throws SQLException {
        Fine f = new Fine();
        f.setFineId(rs.getInt("fine_id"));
        f.setLendingId(rs.getInt("lending_id"));
        f.setArticleId(rs.getInt("article_id"));

        Date iDate = rs.getDate("issue_date");
        if (iDate != null) f.setIssueDate(iDate.toLocalDate());

        Date pDate = rs.getDate("payment_date");
        if (pDate != null) f.setPaymentDate(pDate.toLocalDate());

        f.setPaid(rs.getBoolean("is_paid"));
        f.setAmount(rs.getBigDecimal("amount"));
        f.setComment(rs.getString("comment"));
        return f;
    }
}