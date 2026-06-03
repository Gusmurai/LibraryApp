package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.DeliveryDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Delivery;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeliveryDaoImpl implements DeliveryDao {

    @Override
    public void registerDelivery(String isbn, String supplierInn, double price, int quantity) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psCopy = conn.prepareStatement(SqlProvider.get("delivery.insertCopy"));
                 PreparedStatement psDelivery = conn.prepareStatement(SqlProvider.get("delivery.insert"))) {
                for (int i = 0; i < quantity; i++) {
                    psCopy.setString(1, isbn);
                    psCopy.setDouble(2, price);
                    try (ResultSet rs = psCopy.executeQuery()) {
                        if (rs.next()) {
                            int inventoryNumber = rs.getInt(1);
                            if (supplierInn == null || supplierInn.isBlank()) {
                                psDelivery.setNull(1, Types.VARCHAR);
                            } else {
                                psDelivery.setString(1, supplierInn);
                            }
                            psDelivery.setInt(2, inventoryNumber);
                            psDelivery.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
            log.info("Delivery registered. ISBN={}, quantity={}.", isbn, quantity);
        } catch (SQLException e) {
            rollback(conn);
            log.error("Failed to register delivery for ISBN={}.", isbn, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    @Override
    public List<Delivery> findAll() {
        List<Delivery> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("delivery.findAll"))) {
            while (rs.next()) {
                list.add(mapDelivery(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to load deliveries.", e);
        }
        return list;
    }

    @Override
    public List<Delivery> findWithFilters(LocalDate start, LocalDate end, String supplierInn) {
        List<Delivery> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SqlProvider.get("delivery.findWithFilters.base"));
        if (start != null) appendSql(sql, "delivery.filter.start");
        if (end != null) appendSql(sql, "delivery.filter.end");
        boolean filterSupplier = supplierInn != null && !supplierInn.isBlank()
                && !"Все".equalsIgnoreCase(supplierInn)
                && !"All".equalsIgnoreCase(supplierInn)
                && !"Alle".equalsIgnoreCase(supplierInn);
        if (filterSupplier) appendSql(sql, "delivery.filter.supplier");
        appendSql(sql, "delivery.order");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (start != null) ps.setDate(index++, Date.valueOf(start));
            if (end != null) ps.setDate(index++, Date.valueOf(end));
            if (filterSupplier) ps.setString(index, supplierInn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDelivery(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load deliveries with filters. SQL={}", sql, e);
        }
        return list;
    }

    private void appendSql(StringBuilder sql, String key) {
        sql.append(' ').append(SqlProvider.get(key));
    }

    private Delivery mapDelivery(ResultSet rs) throws SQLException {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(rs.getInt("delivery_id"));
        delivery.setSupplierInn(rs.getString("supplier_inn"));
        delivery.setInventoryNumber(rs.getInt("inventory_number"));
        Date date = rs.getDate("delivery_date");
        if (date != null) {
            delivery.setDeliveryDate(date.toLocalDate());
        }
        return delivery;
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.warn("Rollback failed.", e);
            }
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Connection close failed.", e);
            }
        }
    }
}
