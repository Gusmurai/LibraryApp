package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.dao.SupplierDao;
import ru.library.libraryapp.domains.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * Реализация слоя доступа к данным для работы с таблицами и процедурами БД.
 */

@Slf4j
public class SupplierDaoImpl implements SupplierDao {

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();

        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("supplier.findAll"))) {

            while (rs.next()) {
                suppliers.add(mapSupplier(rs));
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить поставщиков.", e);
        }
        return suppliers;
    }

    @Override
    public Optional<Supplier> findByInn(String inn) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("supplier.findByInn"))) {

            ps.setString(1, inn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSupplier(rs));
            }
        } catch (SQLException e) {
            log.error("Не удалось найти поставщика по ИНН {}.", inn, e);
        }
        return Optional.empty();
    }

    private Supplier mapSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setInn(rs.getString("inn"));
        s.setName(rs.getString("name"));
        s.setAddress(rs.getString("address"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        return s;
    }
}
