package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.ReaderDao;
import ru.library.libraryapp.domains.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class ReaderDaoImpl implements ReaderDao {

    private final Properties sqlProps = new Properties();

    public ReaderDaoImpl() {
        try (InputStream is = getClass().getResourceAsStream("/ru/library/libraryapp/statements.properties")) {
            if (is == null) throw new RuntimeException("Файл statements.properties не найден!");
            sqlProps.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Ошибка загрузки SQL для DAO читателей.", e);
        }
    }

    @Override
    public void add(Reader reader) {
        String sql = sqlProps.getProperty("reader.add");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reader.getLastName());
            ps.setString(2, reader.getFirstName());
            ps.setString(3, reader.getPatronymic());
            ps.setDate(4, reader.getBirthDate() != null ? Date.valueOf(reader.getBirthDate()) : null);
            ps.setString(5, reader.getPassportSeries());
            ps.setString(6, reader.getPassportNumber());
            ps.setString(7, reader.getAddress());
            ps.setString(8, reader.getPhone());
            ps.setBytes(9, reader.getPhoto());
            ps.executeUpdate();
            log.info("Добавлен читатель: {}", reader.getLastName());
        } catch (SQLException e) {
            log.error("Ошибка SQL при добавлении читателя: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void update(Reader reader) {
        String sql = sqlProps.getProperty("reader.update");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reader.getLastName());
            ps.setString(2, reader.getFirstName());
            ps.setString(3, reader.getPatronymic());
            ps.setDate(4, reader.getBirthDate() != null ? Date.valueOf(reader.getBirthDate()) : null);
            ps.setString(5, reader.getPassportSeries());
            ps.setString(6, reader.getPassportNumber());
            ps.setString(7, reader.getAddress());
            ps.setString(8, reader.getPhone());
            ps.setBytes(9, reader.getPhoto());
            ps.setInt(10, reader.getTicketNumber());
            ps.executeUpdate();
            log.info("Обновлен читатель №{}", reader.getTicketNumber());
        } catch (SQLException e) {
            log.error("Ошибка SQL при обновлении читателя: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Optional<Reader> findByTicketNumber(Integer ticketNumber) {
        String sql = sqlProps.getProperty("reader.findByTicketNumber");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapResultSetToReader(rs));
        } catch (SQLException e) { log.error("Ошибка SQL при поиске читателя по номеру билета: {}", e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> list = new ArrayList<>();
        String sql = sqlProps.getProperty("reader.findAll");
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToReader(rs));
        } catch (SQLException e) { log.error("Ошибка SQL при загрузке списка читателей: {}", e.getMessage()); }
        return list;
    }

    @Override
    public List<Reader> searchReaders(String searchQuery) {
        List<Reader> results = new ArrayList<>();
        String sql = sqlProps.getProperty("reader.search");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 1. Первый параметр - для поиска по номеру билета (точное совпадение строки)
            ps.setString(1, searchQuery.trim());

            // 2. Второй параметр - для поиска по ФИО (ищем вхождение подстроки)
            // Мы добавляем %, чтобы находило, даже если ввели только "Иванов Иван" без отчества
            ps.setString(2, "%" + searchQuery.trim() + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToReader(rs));
            }

            log.debug("Поиск по ФИО/Билету завершен. Найдено: {}", results.size());

        } catch (SQLException e) {
            log.error("Ошибка поиска читателей: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return results;
    }

    @Override
    public void changeStatus(Integer ticketNumber, boolean isActive) {
        String sql = sqlProps.getProperty("reader.changeStatus");
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, ticketNumber);
            cs.execute();
            log.info("Статус билета №{} изменен через процедуру", ticketNumber);
        } catch (SQLException e) {
            log.error("Ошибка SQL при изменении статуса читателя: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Reader mapResultSetToReader(ResultSet rs) throws SQLException {
        Reader r = new Reader();
        r.setTicketNumber(rs.getInt("ticket_number"));
        r.setLastName(rs.getString("last_name"));
        r.setFirstName(rs.getString("first_name"));
        r.setPatronymic(rs.getString("patronymic"));
        Date bDate = rs.getDate("birth_date");
        if (bDate != null) r.setBirthDate(bDate.toLocalDate());
        r.setPassportSeries(rs.getString("passport_series"));
        r.setPassportNumber(rs.getString("passport_number"));
        r.setAddress(rs.getString("address"));
        r.setPhone(rs.getString("phone"));
        Date rDate = rs.getDate("registration_date");
        if (rDate != null) r.setRegistrationDate(rDate.toLocalDate());
        r.setPhoto(rs.getBytes("photo"));
        r.setActive(rs.getBoolean("is_active"));
        return r;
    }
}
