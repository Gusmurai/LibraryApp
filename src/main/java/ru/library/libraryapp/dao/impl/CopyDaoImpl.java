package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.CopyDao;
import ru.library.libraryapp.domains.Copy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CopyDaoImpl implements CopyDao {

    @Override
    public List<Copy> findByIsbn(String isbn) {
        List<Copy> list = new ArrayList<>();
        // Используем view_copies_status, чтобы сразу видеть "Выдан", "В наличии" и т.д.
        String sql = "SELECT * FROM view_copies_status WHERE isbn = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Copy c = new Copy();
                c.setInventoryNumber(rs.getInt("inventory_number"));
                c.setIsbn(rs.getString("isbn"));
                c.setCost(rs.getBigDecimal("cost"));
                // Можешь добавить поле status в Copy, чтобы хранить строку из View
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<Copy> findByInventoryNumber(Integer invNumber) {
        String sql = "SELECT * FROM copies WHERE inventory_number = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapCopy(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Copy> searchAvailableByInv(String isbn, String invQuery) {
        List<Copy> list = new ArrayList<>();
        // Выбираем только те экземпляры, которых нет в долгах и нет в списании
        String sql = "SELECT * FROM copies c WHERE c.isbn = ? " +
                "AND CAST(c.inventory_number AS TEXT) ILIKE ? " +
                "AND NOT EXISTS (SELECT 1 FROM lendings l WHERE l.inventory_number = c.inventory_number AND l.return_date IS NULL) " +
                "AND NOT EXISTS (SELECT 1 FROM write_offs w WHERE w.inventory_number = c.inventory_number)";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            ps.setString(2, "%" + invQuery + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapCopy(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Оформление поставки (реализовано через вызов хранимой процедуры или пакетную вставку)
     * В данном случае используем пакетную вставку для гибкости.
     */
    @Override
    public void addDelivery(String isbn, Integer supplierId, double price, int quantity) {
        String sql = "INSERT INTO copies (isbn, cost) VALUES (?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < quantity; i++) {
                ps.setString(1, isbn);
                ps.setDouble(2, price);
                ps.addBatch();
            }
            ps.executeBatch();

            // Здесь же можно добавить логику записи в таблицу deliveries, если нужно
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeOff(Integer invNumber, Integer librarianId, Integer reasonId) {
        String sql = "INSERT INTO write_offs (inventory_number, tabel_number, reason_id, write_off_date) VALUES (?, ?, ?, CURRENT_DATE)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invNumber);
            ps.setInt(2, librarianId);
            ps.setInt(3, reasonId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Copy mapCopy(ResultSet rs) throws SQLException {
        Copy copy = new Copy();
        copy.setInventoryNumber(rs.getInt("inventory_number"));
        copy.setIsbn(rs.getString("isbn"));
        copy.setCost(rs.getBigDecimal("cost"));
        return copy;
    }
}