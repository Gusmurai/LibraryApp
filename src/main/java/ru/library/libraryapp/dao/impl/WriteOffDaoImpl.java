package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.dao.WriteOffDao;
import ru.library.libraryapp.domains.WriteOff;

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
public class WriteOffDaoImpl implements WriteOffDao {

    @Override
    public void create(WriteOff writeOff) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("writeOff.create"))) {
            cs.setInt(1, writeOff.getInventoryNumber());
            cs.setInt(2, writeOff.getTabelNumber());
            cs.setInt(3, writeOff.getReasonId());
            cs.execute();
            log.info("Write-off created for copy {}.", writeOff.getInventoryNumber());
        } catch (SQLException e) {
            log.error("Failed to create write-off.", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<WriteOff> findWithFilters(String query, LocalDate start, LocalDate end) {
        List<WriteOff> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SqlProvider.get("writeOff.findWithFilters.base"));
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            appendSql(sql, "writeOff.findWithFilters.query");
            String like = "%" + query.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (start != null) {
            appendSql(sql, "writeOff.findWithFilters.startDate");
            params.add(Date.valueOf(start));
        }
        if (end != null) {
            appendSql(sql, "writeOff.findWithFilters.endDate");
            params.add(Date.valueOf(end));
        }
        appendSql(sql, "writeOff.findWithFilters.order");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWriteOff(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load write-offs. SQL={}", sql, e);
        }
        return list;
    }

    @Override
    public void restore(Integer inventoryNumber) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("writeOff.restore"))) {
            cs.setInt(1, inventoryNumber);
            cs.execute();
            log.info("Copy {} restored to fund.", inventoryNumber);
        } catch (SQLException e) {
            log.error("Failed to restore copy {}.", inventoryNumber, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void createWriteOff(Integer inventoryNumber, Integer librarianId, Integer reasonId, String comment) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("writeOff.createWithComment"))) {
            ps.setInt(1, inventoryNumber);
            ps.setInt(2, librarianId);
            ps.setInt(3, reasonId);
            ps.setString(4, comment);
            ps.executeUpdate();
            log.info("Write-off with comment created for copy {}.", inventoryNumber);
        } catch (SQLException e) {
            log.error("Failed to create write-off with comment.", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void appendSql(StringBuilder sql, String key) {
        sql.append(' ').append(SqlProvider.get(key));
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private WriteOff mapWriteOff(ResultSet rs) throws SQLException {
        WriteOff writeOff = new WriteOff();
        writeOff.setWriteOffId(rs.getInt("write_off_id"));
        writeOff.setInventoryNumber(rs.getInt("inventory_number"));
        writeOff.setWriteOffDate(toLocalDate(rs.getDate("write_off_date")));
        writeOff.setBookTitle(rs.getString("book_title"));
        writeOff.setIsbn(rs.getString("isbn"));
        writeOff.setReason(rs.getString("reason"));
        String librarianLogin = getStringIfPresent(rs, "librarian_login");
        if (librarianLogin != null && !librarianLogin.isBlank()) {
            writeOff.setLibrarianName(librarianLogin);
        } else {
            writeOff.setLibrarianName(getStringIfPresent(rs, "librarian_name"));
        }
        writeOff.setCost(rs.getBigDecimal("cost"));
        return writeOff;
    }

    private String getStringIfPresent(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return rs.getString(column);
            }
        }
        return null;
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
