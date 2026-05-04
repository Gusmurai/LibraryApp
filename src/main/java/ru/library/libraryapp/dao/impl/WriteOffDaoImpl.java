package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.WriteOffDao;
import ru.library.libraryapp.domains.WriteOff;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WriteOffDaoImpl implements WriteOffDao {

    @Override
    public void create(WriteOff writeOff) {
        String sql = "CALL sp_write_off_copy(?, ?, ?)";
        try (Connection conn = DbConnector.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, writeOff.getInventoryNumber());
            cs.setInt(2, writeOff.getTabelNumber());
            cs.setInt(3, writeOff.getReasonId());
            cs.execute();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }


    @Override
    public List<WriteOff> findWithFilters(String query, LocalDate start, LocalDate end) {
        List<WriteOff> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT w.* FROM write_offs w " +
                        "JOIN copies c ON w.inventory_number = c.inventory_number " +
                        "JOIN books b ON c.isbn = b.isbn " +
                        "WHERE 1=1"
        );

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (b.title ILIKE ? OR CAST(w.inventory_number AS TEXT) ILIKE ?)");
        }
        if (start != null) sql.append(" AND w.write_off_date >= ?");
        if (end != null) sql.append(" AND w.write_off_date <= ?");

        sql.append(" ORDER BY w.write_off_date DESC");

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (query != null && !query.isEmpty()) {
                String q = "%" + query + "%";
                ps.setString(paramIndex++, q);
                ps.setString(paramIndex++, q);
            }
            if (start != null) ps.setDate(paramIndex++, Date.valueOf(start));
            if (end != null) ps.setDate(paramIndex++, Date.valueOf(end));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapWriteOff(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void restore(Integer inventoryNumber) {
        // В процедуре мы используем ID акта или инв. номер.
        // Если в базе процедура sp_restore_copy(p_inventory_number):
        String sql = "CALL sp_restore_copy(?)";
        try (Connection conn = DbConnector.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, inventoryNumber);
            cs.execute();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    private WriteOff mapWriteOff(ResultSet rs) throws SQLException {
        WriteOff w = new WriteOff();
        w.setWriteOffId(rs.getInt("write_off_id"));
        w.setInventoryNumber(rs.getInt("inventory_number"));
        w.setTabelNumber(rs.getInt("tabel_number"));
        w.setReasonId(rs.getInt("reason_id"));

        Date d = rs.getDate("write_off_date");
        if (d != null) w.setWriteOffDate(d.toLocalDate());

        return w;
    }
}