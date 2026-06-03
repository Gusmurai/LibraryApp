package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.AuditLogDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AuditLogDaoImpl implements AuditLogDao {

    @Override
    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("audit.findRecent"))) {

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
            log.error("Не удалось загрузить последние записи аудита.", e);
        }
        return logs;
    }
}
