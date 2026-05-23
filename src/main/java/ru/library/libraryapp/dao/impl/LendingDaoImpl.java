package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.LendingDao;
import ru.library.libraryapp.domains.Lending;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LendingDaoImpl implements LendingDao {

    @Override
    public void issueBook(Integer ticketNumber, Integer inventoryNumber, Integer librarianId) {
        String sql = "CALL sp_issue_book(?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, ticketNumber);
            cs.setInt(2, inventoryNumber);
            cs.setInt(3, librarianId);
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void returnBook(Integer lendingId, Integer librarianId) {
        String sql = "{call sp_return_book(?, ?, ?)}";

        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            // Входной параметр (ID выдачи)
            cs.setInt(1, lendingId);

            // Регистрация выходных параметров (Просрочка и Сообщение)
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();

            // Получаем результаты из OUT-параметров
            int overdueDays = cs.getInt(2);
            String statusMessage = cs.getString(3);

            // Если просрочка есть, прерываем выполнение исключением с текстом от БД
            if (overdueDays > 0) {
                throw new RuntimeException(statusMessage);
            }

        } catch (SQLException e) {
            // Пробрасываем системные ошибки БД (RAISE EXCEPTION)
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void renewBook(Integer lendingId) {
        String sql = "CALL sp_extend_book(?)";
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, lendingId);
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void markAsLost(Integer lendingId, Integer librarianId) {
        // Логика: если книга утеряна, мы закрываем выдачу текущей датой
        // (в базе сработают триггеры на штраф и списание, если они настроены,
        // либо мы вызовем соответствующие DAO методы в контроллере)
        String sql = "UPDATE lendings SET return_date = CURRENT_DATE WHERE lending_id = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lendingId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Lending> findActiveByReader(Integer ticketNumber) {
        List<Lending> list = new ArrayList<>();
        String sql = "SELECT * FROM lendings WHERE ticket_number = ? AND return_date IS NULL ORDER BY lend_date DESC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapLending(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Lending> findHistoryByInventoryNumber(Integer inventoryNumber) {
        List<Lending> list = new ArrayList<>();
        String sql = "SELECT * FROM lendings WHERE inventory_number = ? ORDER BY lend_date DESC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventoryNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapLending(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Lending> findHistoryByReader(Integer ticketNumber) {
        List<Lending> list = new ArrayList<>();
        String sql = "SELECT * FROM lendings WHERE ticket_number = ? ORDER BY lend_date DESC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapLending(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Lending mapLending(ResultSet rs) throws SQLException {
        Lending l = new Lending();
        l.setLendingId(rs.getInt("lending_id"));
        l.setInventoryNumber(rs.getInt("inventory_number"));
        l.setTicketNumber(rs.getInt("ticket_number"));
        l.setTabelNumber(rs.getInt("tabel_number"));

        Date lendDate = rs.getDate("lend_date");
        if (lendDate != null) l.setLendDate(lendDate.toLocalDate());

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) l.setDueDate(dueDate.toLocalDate());

        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) l.setReturnDate(returnDate.toLocalDate());

        l.setRenewalsCount(rs.getInt("renewals_count"));
        return l;
    }
}