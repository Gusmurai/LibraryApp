package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.CopyDao;
import ru.library.libraryapp.domains.Copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;/**
 * Реализация слоя доступа к данным для работы с таблицами и процедурами БД.
 */


@Slf4j
public class CopyDaoImpl implements CopyDao {

    private final Properties sqlProps = new Properties();

    public CopyDaoImpl() {
        try (InputStream is = getClass().getResourceAsStream("/ru/library/libraryapp/statements.properties")) {
            if (is == null) throw new RuntimeException("Файл statements.properties не найден!");
            sqlProps.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Ошибка загрузки SQL для DAO экземпляров.", e);
        }
    }

    @Override
    public List<Copy> findByIsbn(String isbn) {
        List<Copy> list = new ArrayList<>();
        String sql = sqlProps.getProperty("copy.findByIsbn");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapCopy(rs));
            }
        } catch (SQLException e) { log.error("Ошибка SQL при поиске экземпляров по ISBN: {}", e.getMessage()); }
        return list;
    }

    @Override
    public Optional<Copy> findByInventoryNumber(Integer invNumber) {
        String sql = sqlProps.getProperty("copy.findByInv");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapCopy(rs));
        } catch (SQLException e) { log.error("Ошибка SQL при поиске экземпляра по инвентарному номеру: {}", e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<Copy> searchAvailable(String isbn, String invQuery) {
        List<Copy> list = new ArrayList<>();
        String sql = sqlProps.getProperty("copy.searchAvailable");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setString(2, "%" + invQuery + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCopy(rs));
        } catch (SQLException e) { log.error("Ошибка SQL при поиске доступных экземпляров: {}", e.getMessage()); }
        return list;
    }

    @Override
    public void addDelivery(String isbn, Integer supplierId, double price, int quantity) {
        // Здесь можно либо оставить логику batch, либо вызвать процедуру, если она есть
        String sql = sqlProps.getProperty("copy.addBatch");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < quantity; i++) {
                ps.setString(1, isbn);
                ps.setDouble(2, price);
                ps.addBatch();
            }
            ps.executeBatch();
            log.info("Поставка оформлена: {} шт. для ISBN {}", quantity, isbn);
        } catch (SQLException e) { log.error("Ошибка SQL при оформлении поставки: {}", e.getMessage()); }
    }

    @Override
    public void writeOff(Integer invNumber, Integer librarianId, Integer reasonId) {
        String sql = sqlProps.getProperty("copy.writeOff");
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, invNumber);
            cs.setInt(2, librarianId);
            cs.setInt(3, reasonId);
            cs.execute();
            log.info("Экземпляр №{} списан через процедуру", invNumber);
        } catch (SQLException e) {
            log.error("Ошибка SQL при списании экземпляра: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private Copy mapCopy(ResultSet rs) throws SQLException {
        Copy copy = new Copy();
        copy.setInventoryNumber(rs.getInt("inventory_number"));
        copy.setIsbn(rs.getString("isbn"));
        copy.setCost(rs.getBigDecimal("cost"));
        copy.setStatus(rs.getString("status"));

        return copy;
    }
}
