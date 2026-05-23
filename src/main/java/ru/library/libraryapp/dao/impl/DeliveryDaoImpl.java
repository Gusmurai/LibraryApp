package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.DeliveryDao;
import ru.library.libraryapp.domains.Delivery;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDaoImpl implements DeliveryDao {

    @Override
    public void registerDelivery(String isbn, String supplierInn, double price, int quantity) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); // Начинаем транзакцию

            String sqlCopy = "INSERT INTO copies (isbn, cost) VALUES (?, ?) RETURNING inventory_number";
            String sqlDelivery = "INSERT INTO deliveries (supplier_inn, inventory_number, delivery_date) VALUES (?, ?, CURRENT_DATE)";

            try (PreparedStatement psCopy = conn.prepareStatement(sqlCopy);
                 PreparedStatement psDeliv = conn.prepareStatement(sqlDelivery)) {

                for (int i = 0; i < quantity; i++) {
                    // 1. Создаем экземпляр
                    psCopy.setString(1, isbn);
                    psCopy.setDouble(2, price);

                    ResultSet rs = psCopy.executeQuery();
                    if (rs.next()) {
                        int newInvNumber = rs.getInt(1);

                        // 2. Создаем запись о поставке для этого экземпляра
                        if (supplierInn == null || supplierInn.isEmpty()) {
                            psDeliv.setNull(1, Types.VARCHAR);
                        } else {
                            psDeliv.setString(1, supplierInn);
                        }
                        psDeliv.setInt(2, newInvNumber);
                        psDeliv.executeUpdate();
                    }
                }
            }
            conn.commit(); // Подтверждаем всё разом
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    @Override
    public List<Delivery> findAll() {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT * FROM deliveries ORDER BY delivery_date DESC";
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapDelivery(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Delivery> findWithFilters(LocalDate start, LocalDate end, String supplierInn) {
        List<Delivery> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM deliveries WHERE 1=1");

        if (start != null) sql.append(" AND delivery_date >= ?");
        if (end != null) sql.append(" AND delivery_date <= ?");
        if (supplierInn != null && !supplierInn.equals("Все")) sql.append(" AND supplier_inn = ?");

        sql.append(" ORDER BY delivery_date DESC");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (start != null) ps.setDate(paramIndex++, Date.valueOf(start));
            if (end != null) ps.setDate(paramIndex++, Date.valueOf(end));
            if (supplierInn != null && !supplierInn.equals("Все")) ps.setString(paramIndex, supplierInn);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDelivery(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Delivery mapDelivery(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setDeliveryId(rs.getInt("delivery_id"));
        d.setSupplierInn(rs.getString("supplier_inn"));
        d.setInventoryNumber(rs.getInt("inventory_number"));

        Date date = rs.getDate("delivery_date");
        if (date != null) d.setDeliveryDate(date.toLocalDate());

        return d;
    }
}