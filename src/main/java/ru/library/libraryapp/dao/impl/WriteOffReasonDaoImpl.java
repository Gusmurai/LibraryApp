package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.dao.WriteOffReasonDao;
import ru.library.libraryapp.domains.WriteOffReason;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * Реализация слоя доступа к данным для работы с таблицами и процедурами БД.
 */

@Slf4j
public class WriteOffReasonDaoImpl implements WriteOffReasonDao {
    @Override
    public List<WriteOffReason> findAll() {
        List<WriteOffReason> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("writeOffReason.findAll"))) {
            while (rs.next()) {
                list.add(mapReason(rs));
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить причины списания.", e);
        }
        return list;
    }

    @Override
    public Optional<WriteOffReason> findById(Integer id) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("writeOffReason.findById"))) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapReason(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить причину списания {}.", id, e);
        }
        return Optional.empty();
    }

    private WriteOffReason mapReason(ResultSet rs) throws SQLException {
        WriteOffReason reason = new WriteOffReason();
        reason.setReasonId(rs.getInt("reason_id"));
        reason.setName(rs.getString("name"));
        return reason;
    }
}
