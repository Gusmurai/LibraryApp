package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.FineDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Fine;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FineDaoImpl implements FineDao {

    @Override
    public List<Fine> findUnpaidByReader(Integer ticketNumber) {
        List<Fine> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.findUnpaidByReader"))) {
            ps.setInt(1, ticketNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFine(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load unpaid fines for reader {}.", ticketNumber, e);
        }
        return list;
    }

    @Override
    public List<Fine> findWithFilters(String query, LocalDate start, LocalDate end, Integer articleId, Boolean isPaid) {
        List<Fine> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SqlProvider.get("fine.findWithFilters.base"));
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            appendSql(sql, "fine.findWithFilters.query");
            String like = "%" + query.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(query.trim());
        }
        if (start != null) {
            appendSql(sql, "fine.findWithFilters.startDate");
            params.add(Date.valueOf(start));
        }
        if (end != null) {
            appendSql(sql, "fine.findWithFilters.endDate");
            params.add(Date.valueOf(end));
        }
        if (articleId != null) {
            appendSql(sql, "fine.findWithFilters.article");
            params.add(articleId);
        }
        if (isPaid != null) {
            appendSql(sql, "fine.findWithFilters.paid");
            params.add(isPaid);
        }
        appendSql(sql, "fine.findWithFilters.order");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFine(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load fines with filters. SQL={}", sql, e);
        }
        return list;
    }

    @Override
    public double calculateTotalSum(List<Fine> fines) {
        return fines.stream()
                .filter(f -> f.getAmount() != null)
                .map(f -> f.getAmount().doubleValue())
                .reduce(0.0, Double::sum);
    }

    @Override
    public void payFine(Integer fineId) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("fine.pay"))) {
            cs.setInt(1, fineId);
            cs.execute();
            log.info("Fine {} paid.", fineId);
        } catch (SQLException e) {
            log.error("Failed to pay fine {}.", fineId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void updateNote(Integer fineId, String note) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.updateNote"))) {
            ps.setString(1, note);
            ps.setInt(2, fineId);
            ps.executeUpdate();
            log.info("Fine {} note updated.", fineId);
        } catch (SQLException e) {
            log.error("Failed to update fine {} note.", fineId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void updateAmount(Integer fineId, double newAmount) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("fine.updateAmount"))) {
            cs.setInt(1, fineId);
            cs.setBigDecimal(2, BigDecimal.valueOf(newAmount));
            cs.execute();
            log.info("Fine {} amount changed to {}.", fineId, newAmount);
        } catch (SQLException e) {
            log.error("Failed to update fine {} amount.", fineId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void createFine(Integer lendingId, Integer articleId, double amount, String comment, boolean isPaid) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall(SqlProvider.get("fine.create"))) {
                cs.setInt(1, lendingId);
                cs.setInt(2, articleId);
                cs.setBigDecimal(3, BigDecimal.valueOf(amount));
                cs.setString(4, comment);
                cs.execute();
            }

            if (isPaid) {
                Integer fineId = findLastCreatedFineId(conn, lendingId, articleId);
                if (fineId == null) {
                    throw new SQLException("Created fine id was not found.");
                }
                try (CallableStatement cs = conn.prepareCall(SqlProvider.get("fine.pay"))) {
                    cs.setInt(1, fineId);
                    cs.execute();
                }
            }

            conn.commit();
            log.info("Fine created. Lending={}, article={}, paidImmediately={}.", lendingId, articleId, isPaid);
        } catch (SQLException e) {
            rollbackQuietly(conn);
            log.error("Failed to create fine.", e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void annulFine(Integer fineId, int mode) {
        boolean restoreCopy = mode == 1;
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("fine.annul"))) {
            cs.setInt(1, fineId);
            cs.setBoolean(2, restoreCopy);
            cs.execute();
            log.info("Fine {} annulled. restoreCopy={}.", fineId, restoreCopy);
        } catch (SQLException e) {
            log.error("Failed to annul fine {}.", fineId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public double calculateFineAmount(Integer lendingId, Integer articleId, Integer daysOverdue) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.calculate"))) {
            ps.setInt(1, lendingId);
            ps.setInt(2, articleId);
            ps.setInt(3, daysOverdue != null ? daysOverdue : 0);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            log.warn("Database fine amount calculation failed, fallback will be used.", e);
            return fallbackFineAmount(lendingId, articleId, daysOverdue);
        }
        return 0.0;
    }

    private void appendSql(StringBuilder sql, String key) {
        sql.append(' ').append(SqlProvider.get(key));
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private Integer findLastCreatedFineId(Connection conn, Integer lendingId, Integer articleId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.findLastCreated"))) {
            ps.setInt(1, lendingId);
            ps.setInt(2, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("fine_id") : null;
            }
        }
    }

    private Fine mapFine(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setFineId(rs.getInt("fine_id"));
        setIntegerIfPresent(rs, "lending_id", fine::setLendingId);
        setIntegerIfPresent(rs, "article_id", fine::setArticleId);
        setIntegerIfPresent(rs, "ticket_number", fine::setTicketNumber);
        fine.setIssueDate(toLocalDate(rs.getDate("issue_date")));
        fine.setPaymentDate(toLocalDate(rs.getDate("payment_date")));
        fine.setPaid(rs.getBoolean("is_paid"));
        fine.setAmount(rs.getBigDecimal("amount"));
        fine.setComment(rs.getString("comment"));
        setStringIfPresent(rs, "article_name", fine::setArticleName);
        setStringIfPresent(rs, "book_title", fine::setBookTitle);
        setStringIfPresent(rs, "reader_fullname", fine::setReaderFullName);
        return fine;
    }

    private double fallbackFineAmount(Integer lendingId, Integer articleId, Integer daysOverdue) {
        if (articleId == 1 && daysOverdue != null && daysOverdue > 0) {
            return 5.0 * daysOverdue;
        }
        if (articleId == 3) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.copyCostByLending"))) {
                ps.setInt(1, lendingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble(1) + 100.0;
                    }
                }
            } catch (SQLException e) {
                log.warn("Lost-copy fine fallback for current schema failed.", e);
                Double amount = lostCopyAmountFallback(lendingId);
                if (amount != null) {
                    return amount;
                }
            }
            return 500.0;
        }
        return 100.0;
    }

    private Double lostCopyAmountFallback(Integer lendingId) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("fine.copyCostByLendingNewSchema"))) {
            ps.setInt(1, lendingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1) + 100.0;
                }
            }
        } catch (SQLException e) {
            log.warn("Lost-copy fine fallback for new schema failed.", e);
        }
        return null;
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.warn("Transaction rollback failed.", e);
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Connection close failed.", e);
            }
        }
    }

    private interface IntegerSetter {
        void set(Integer value);
    }

    private interface StringSetter {
        void set(String value);
    }

    private void setIntegerIfPresent(ResultSet rs, String column, IntegerSetter setter) throws SQLException {
        if (hasColumn(rs, column)) {
            int value = rs.getInt(column);
            if (!rs.wasNull()) {
                setter.set(value);
            }
        }
    }

    private void setStringIfPresent(ResultSet rs, String column, StringSetter setter) throws SQLException {
        if (hasColumn(rs, column)) {
            setter.set(rs.getString(column));
        }
    }

    private boolean hasColumn(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
