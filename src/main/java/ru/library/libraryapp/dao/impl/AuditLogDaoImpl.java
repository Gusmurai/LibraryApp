package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.AuditLogDao;
import ru.library.libraryapp.domains.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDaoImpl implements AuditLogDao {

    @Override
    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY operation_time DESC LIMIT ?";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setLogId(rs.getInt("log_id"));
                log.setTableName(rs.getString("table_name"));
                log.setOperationType(rs.getString("operation_type"));

                Timestamp ts = rs.getTimestamp("operation_time");
                if (ts != null) log.setOperationTime(ts.toLocalDateTime());

                log.setDbUser(rs.getString("db_user"));
                log.setOldData(rs.getString("old_data"));
                log.setNewData(rs.getString("new_data"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}