package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DbConnector;
import ru.library.libraryapp.dao.BookDao;
import ru.library.libraryapp.domains.Author;
import ru.library.libraryapp.domains.Book;
import ru.library.libraryapp.domains.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDaoImpl implements BookDao {

    @Override
    public void add(Book book) {
        String sql = "INSERT INTO books (isbn, title, publication_year, page_count, bbk, author_mark, publisher_id, cover_image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setObject(3, book.getPublicationYear()); // setObject для поддержки null
            ps.setInt(4, book.getPageCount());
            ps.setString(5, book.getBbk());
            ps.setString(6, book.getAuthorMark());
            ps.setObject(7, book.getPublisherId());
            ps.setBytes(8, book.getCoverImage());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void update(Book book) {
        String sql = "UPDATE books SET title = ?, publication_year = ?, page_count = ?, bbk = ?, " +
                "author_mark = ?, publisher_id = ?, cover_image = ? WHERE isbn = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setObject(2, book.getPublicationYear());
            ps.setInt(3, book.getPageCount());
            ps.setString(4, book.getBbk());
            ps.setString(5, book.getAuthorMark());
            ps.setObject(6, book.getPublisherId());
            ps.setBytes(7, book.getCoverImage());
            ps.setString(8, book.getIsbn());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapBook(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        // Берем данные из представления!
        String sql = "SELECT * FROM view_book_catalog WHERE is_active = true";
        try (Connection conn = DbConnector.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Book b = new Book();
                b.setIsbn(rs.getString("isbn"));
                b.setTitle(rs.getString("title"));
                // Мы можем добавить в класс Book поля authorsList и genresList (String),
                // чтобы просто выводить то, что прислал View
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Book> searchBooks(String query) {
        List<Book> list = new ArrayList<>();
        // Сложный поиск: по ISBN, Названию или Фамилии автора через JOIN
        String sql = "SELECT DISTINCT b.* FROM books b " +
                "LEFT JOIN book_authors ba ON b.isbn = ba.isbn " +
                "LEFT JOIN authors a ON ba.author_id = a.author_id " +
                "WHERE b.isbn ILIKE ? OR b.title ILIKE ? OR a.last_name ILIKE ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBook(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void changeStatus(String isbn, boolean isActive) {
        String sql = "CALL sp_change_book_status(?)";
        try (Connection conn = DbConnector.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, isbn);
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public int getAvailableCount(String isbn) {
        // Считаем те экземпляры, которые НЕ выданы (нет записи в lendings с пустым return_date)
        String sql = "SELECT count(*) FROM copies c " +
                "WHERE c.isbn = ? AND c.inventory_number NOT IN " +
                "(SELECT inventory_number FROM lendings WHERE return_date IS NULL)";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int getTotalCount(String isbn) {
        String sql = "SELECT count(*) FROM copies WHERE isbn = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public List<Author> getAuthorsByIsbn(String isbn) {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT a.* FROM authors a JOIN book_authors ba ON a.author_id = ba.author_id WHERE ba.isbn = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Author a = new Author();
                a.setAuthorId(rs.getInt("author_id"));
                a.setLastName(rs.getString("last_name"));
                a.setFirstName(rs.getString("first_name"));
                authors.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return authors;
    }

    @Override
    public List<Genre> getGenresByIsbn(String isbn) {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT g.* FROM genres g JOIN book_genres bg ON g.genre_id = bg.genre_id WHERE bg.isbn = ?";
        try (Connection conn = DbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Genre g = new Genre();
                g.setGenreId(rs.getInt("genre_id"));
                g.setName(rs.getString("name"));
                genres.add(g);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return genres;
    }

    @Override
    public void updateAuthors(String isbn, List<Integer> authorIds) {
        try (Connection conn = DbConnector.getConnection()) {
            conn.setAutoCommit(false); // Начинаем транзакцию
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM book_authors WHERE isbn = ?")) {
                psDel.setString(1, isbn);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement("INSERT INTO book_authors (isbn, author_id) VALUES (?, ?)")) {
                for (Integer id : authorIds) {
                    psIns.setString(1, isbn);
                    psIns.setInt(2, id);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
            conn.commit(); // Завершаем транзакцию
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void updateGenres(String isbn, List<Integer> genreIds) {
        try (Connection conn = DbConnector.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM book_genres WHERE isbn = ?")) {
                psDel.setString(1, isbn);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement("INSERT INTO book_genres (isbn, genre_id) VALUES (?, ?)")) {
                for (Integer id : genreIds) {
                    psIns.setString(1, isbn);
                    psIns.setInt(2, id);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setPublicationYear(rs.getInt("publication_year"));
        b.setPageCount(rs.getInt("page_count"));
        b.setBbk(rs.getString("bbk"));
        b.setAuthorMark(rs.getString("author_mark"));
        b.setPublisherId(rs.getInt("publisher_id"));
        b.setCoverImage(rs.getBytes("cover_image"));
        b.setActive(rs.getBoolean("is_active"));
        return b;
    }
}