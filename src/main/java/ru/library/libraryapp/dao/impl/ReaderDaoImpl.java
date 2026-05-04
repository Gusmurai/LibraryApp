package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
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

public class ReaderDaoImpl implements ReaderDao {

    private final Properties sqlProps = new Properties();

    public ReaderDaoImpl() {
        try (InputStream is = getClass().getResourceAsStream("/ru/library/libraryapp/statements.properties")) {
            if (is == null) {
                throw new RuntimeException("Файл statements.properties не найден!");
            }
            sqlProps.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки SQL-запросов: " + e.getMessage());
        }
    }

    @Override
    public void add(Reader reader) {
        String sql = sqlProps.getProperty("reader.add");
        try (Connection conn = DbConnector.getConnection();
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
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void update(Reader reader) {
        String sql = sqlProps.getProperty("reader.update");
        try (Connection conn = DbConnector.getConnection();
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
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Optional<Reader> findByTicketNumber(Integer ticketNumber) {
        String sql = sqlProps.getProperty("reader.findByTicketNumber");
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToReader(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> list = new ArrayList<>();
        String sql = sqlProps.getProperty("reader.findAll");
        try (Connection conn = DbConnector.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToReader(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

    @Override
    public List<Reader> searchReaders(String searchQuery) {
        List<Reader> results = new ArrayList<>();
        String sql = sqlProps.getProperty("reader.search");
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String wrapQuery = "%" + searchQuery + "%";
            ps.setString(1, searchQuery);  // Для поиска по ID
            ps.setString(2, wrapQuery);    // Для поиска по ФИО
            ps.setString(3, wrapQuery);    // Для поиска по телефону

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToReader(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return results;
    }

    @Override
    public void changeStatus(Integer ticketNumber, boolean isActive) {
        String sql = sqlProps.getProperty("reader.changeStatus");
        try (Connection conn = DbConnector.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, ticketNumber);
            cs.execute();
        } catch (SQLException e) {
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