package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.PublisherDao;
import ru.library.libraryapp.domains.Publisher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PublisherDaoImpl implements PublisherDao {
    @Override
    public List<Publisher> findAll() {
        List<Publisher> list = new ArrayList<>();
        try (Connection conn = DbConnector.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM publishers ORDER BY name")) {
            while (rs.next()) {
                Publisher p = new Publisher();
                p.setPublisherId(rs.getInt("publisher_id"));
                p.setName(rs.getString("name"));
                p.setCity(rs.getString("city"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<Publisher> findById(Integer id) {
        String sql = "SELECT * FROM publishers WHERE publisher_id = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Publisher p = new Publisher();
                p.setPublisherId(rs.getInt("publisher_id"));
                p.setName(rs.getString("name"));
                return Optional.of(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
}