package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.LendingDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Lending;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;/**
 * Реализация слоя доступа к данным для работы с таблицами и процедурами БД.
 */


@Slf4j
public class LendingDaoImpl implements LendingDao {

    @Override
    public void issueBook(Integer ticketNumber, Integer inventoryNumber, Integer librarianId) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("lending.issueBook"))) {
            cs.setInt(1, ticketNumber);
            cs.setInt(2, inventoryNumber);
            cs.setInt(3, librarianId);
            cs.execute();
            log.info("Экземпляр {} выдан читателю {}.", inventoryNumber, ticketNumber);
        } catch (SQLException e) {
            log.error("Не удалось выдать экземпляр {} читателю {}.", inventoryNumber, ticketNumber, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void returnBook(Integer lendingId, Integer librarianId) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall(SqlProvider.get("lending.returnBook"))) {
                cs.setInt(1, lendingId);
                cs.registerOutParameter(2, Types.INTEGER);
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.execute();

                int overdueDays = cs.getInt(2);
                String statusMessage = cs.getString(3);
                if (overdueDays > 0) {
                    conn.rollback();
                    throw new RuntimeException("OVERDUE:" + overdueDays + ":" + statusMessage);
                }
                conn.commit();
                log.info("Выдача {} закрыта возвратом.", lendingId);
            }
        } catch (SQLException e) {
            rollbackQuietly(conn);
            log.error("Не удалось оформить возврат выдачи {}.", lendingId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void closeReturn(Integer lendingId) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("lending.closeReturn"))) {
            ps.setInt(1, lendingId);
            ps.executeUpdate();
            log.info("Выдача {} закрыта после обязательного сценария возврата.", lendingId);
        } catch (SQLException e) {
            log.error("Не удалось закрыть выдачу {} после обязательного сценария возврата.", lendingId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void renewBook(Integer lendingId) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("lending.renewBook"))) {
            cs.setInt(1, lendingId);
            cs.execute();
            log.info("Выдача {} продлена.", lendingId);
        } catch (SQLException e) {
            log.error("Не удалось продлить выдачу {}.", lendingId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void markAsLost(Integer lendingId, Integer librarianId) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("lending.markAsLost"))) {
            ps.setInt(1, lendingId);
            ps.executeUpdate();
            log.info("Выдача {} закрыта как утерянная.", lendingId);
        } catch (SQLException e) {
            log.error("Не удалось закрыть выдачу {} как утерянную.", lendingId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Lending> findActiveByReader(Integer ticketNumber) {
        List<Lending> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("lending.findActiveByReader"))) {
            ps.setInt(1, ticketNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLending(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить активные выдачи читателя {}.", ticketNumber, e);
        }
        return list;
    }

    @Override
    public List<Lending> findHistoryByInventoryNumber(Integer inventoryNumber) {
        List<Lending> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("lending.historyByInventoryNumber"))) {
            ps.setInt(1, inventoryNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLending(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить историю экземпляра {}.", inventoryNumber, e);
            return findHistoryByInventoryNumberFallback(inventoryNumber);
        }
        return list;
    }

    @Override
    public List<Lending> findHistoryByReader(Integer ticketNumber) {
        List<Lending> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("lending.historyByReader"))) {
            ps.setInt(1, ticketNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLending(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить историю читателя {}.", ticketNumber, e);
            return findHistoryByReaderFallback(ticketNumber);
        }
        return list;
    }

    private List<Lending> findHistoryByInventoryNumberFallback(Integer inventoryNumber) {
        return loadHistoryFallback(SqlProvider.get("lending.historyByInventoryNumberNewSchema"), inventoryNumber, "copy");
    }

    private List<Lending> findHistoryByReaderFallback(Integer ticketNumber) {
        return loadHistoryFallback(SqlProvider.get("lending.historyByReaderNewSchema"), ticketNumber, "reader");
    }

    private List<Lending> loadHistoryFallback(String sql, Integer value, String type) {
        List<Lending> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLending(rs));
                }
            }
        } catch (SQLException fallbackError) {
            log.error("Резервный запрос истории типа {} не выполнен для {}.", type, value, fallbackError);
        }
        return list;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.warn("Не удалось выполнить откат транзакции.", e);
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Не удалось закрыть соединение с БД.", e);
            }
        }
    }

    private Lending mapLending(ResultSet rs) throws SQLException {
        Lending lending = new Lending();
        setIntegerIfPresent(rs, "lending_id", lending::setLendingId);
        setIntegerIfPresent(rs, "inventory_number", lending::setInventoryNumber);
        setIntegerIfPresent(rs, "copy_inventory_number", lending::setInventoryNumber);
        setIntegerIfPresent(rs, "ticket_number", lending::setTicketNumber);
        setIntegerIfPresent(rs, "reader_ticket_number", lending::setTicketNumber);
        setIntegerIfPresent(rs, "tabel_number", lending::setTabelNumber);
        setIntegerIfPresent(rs, "librarian_tabel_number", lending::setTabelNumber);
        lending.setLendDate(toLocalDate(getDateIfPresent(rs, "lend_date")));
        lending.setDueDate(toLocalDate(getDateIfPresent(rs, "due_date")));
        lending.setReturnDate(toLocalDate(getDateIfPresent(rs, "return_date")));
        setIntegerIfPresent(rs, "renewals_count", lending::setRenewalsCount);
        setStringIfPresent(rs, "book_title", lending::setBookTitle);
        setStringIfPresent(rs, "title", lending::setBookTitle);
        setStringIfPresent(rs, "reader_fullname", lending::setReaderFullName);
        setStringIfPresent(rs, "reader_name", lending::setReaderFullName);
        setStringIfPresent(rs, "reader", lending::setReaderFullName);
        setStringIfPresent(rs, "reader_phone", lending::setReaderPhone);
        setStringIfPresent(rs, "phone", lending::setReaderPhone);
        setStringIfPresent(rs, "librarian_login", lending::setLibrarianName);
        if (lending.getLibrarianName() == null || lending.getLibrarianName().isBlank()) {
            setStringIfPresent(rs, "librarian_name", lending::setLibrarianName);
        }
        if (lending.getLibrarianName() == null || lending.getLibrarianName().isBlank()) {
            setStringIfPresent(rs, "librarian_short", lending::setLibrarianName);
        }
        if (lending.getLibrarianName() == null || lending.getLibrarianName().isBlank()) {
            setStringIfPresent(rs, "employee", lending::setLibrarianName);
        }
        return lending;
    }

    private java.time.LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private Date getDateIfPresent(ResultSet rs, String column) throws SQLException {
        return hasColumn(rs, column) ? rs.getDate(column) : null;
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
