package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.PublisherDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Publisher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class PublisherDaoImpl implements PublisherDao {
    @Override
    public List<Publisher> findAll() {
        List<Publisher> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("publisher.findAll"))) {
            while (rs.next()) {
                Publisher p = new Publisher();
                p.setPublisherId(rs.getInt("publisher_id"));
                p.setName(rs.getString("name"));
                p.setCity(rs.getString("city"));
                list.add(p);
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить издательства.", e);
        }
        return list;
    }

    @Override
    public Optional<Publisher> findById(Integer id) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("publisher.findById"))) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Publisher p = new Publisher();
                p.setPublisherId(rs.getInt("publisher_id"));
                p.setName(rs.getString("name"));
                return Optional.of(p);
            }
        } catch (SQLException e) {
            log.error("Не удалось найти издательство по id {}.", id, e);
        }
        return Optional.empty();
    }
}
