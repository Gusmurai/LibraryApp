package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.WriteOffReasonDao;
import ru.library.libraryapp.domains.WriteOffReason;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WriteOffReasonDaoImpl implements WriteOffReasonDao {
    @Override
    public List<WriteOffReason> findAll() {
        List<WriteOffReason> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM write_off_reasons")) {
            while (rs.next()) {
                WriteOffReason wor = new WriteOffReason();
                wor.setReasonId(rs.getInt("reason_id"));
                wor.setName(rs.getString("name"));
                list.add(wor);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<WriteOffReason> findById(Integer id) {
        String sql = "SELECT * FROM write_off_reasons WHERE reason_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                WriteOffReason wor = new WriteOffReason();
                wor.setReasonId(rs.getInt("reason_id"));
                wor.setName(rs.getString("name"));
                return Optional.of(wor);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
}