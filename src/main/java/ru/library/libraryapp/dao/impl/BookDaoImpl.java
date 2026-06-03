package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.BookDao;
import ru.library.libraryapp.dao.SqlProvider;
import ru.library.libraryapp.domains.Author;
import ru.library.libraryapp.domains.Book;
import ru.library.libraryapp.domains.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BookDaoImpl implements BookDao {

    @Override
    public void add(Book book) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("book.add"))) {
            fillBookStatement(ps, book);
            ps.executeUpdate();
            log.info("Книга {} добавлена.", book.getIsbn());
        } catch (SQLException e) {
            log.error("Не удалось добавить книгу {}.", book.getIsbn(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Book book) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("book.update"))) {
            ps.setString(1, book.getTitle());
            ps.setObject(2, book.getPublicationYear());
            ps.setObject(3, book.getPageCount());
            ps.setString(4, book.getBbk());
            ps.setString(5, book.getAuthorMark());
            ps.setObject(6, book.getPublisherId());
            ps.setBytes(7, book.getCoverImage());
            ps.setString(8, book.getIsbn());
            ps.executeUpdate();
            log.info("Книга {} обновлена.", book.getIsbn());
        } catch (SQLException e) {
            log.error("Не удалось обновить книгу {}.", book.getIsbn(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("book.findByIsbn"))) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapBook(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Не удалось найти книгу по ISBN {}.", isbn, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SqlProvider.get("book.findAll"))) {
            while (rs.next()) {
                list.add(mapBook(rs));
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить книги.", e);
        }
        return list;
    }

    @Override
    public List<Book> searchBooks(String query) {
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("book.search"))) {
            String like = "%" + query + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBook(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось выполнить поиск книг по запросу {}.", query, e);
        }
        return list;
    }

    @Override
    public void changeStatus(String isbn, boolean isActive) {
        try (Connection conn = DBHelper.getConnection();
             CallableStatement cs = conn.prepareCall(SqlProvider.get("book.changeStatus"))) {
            cs.setString(1, isbn);
            cs.execute();
            log.info("Статус книги {} изменен.", isbn);
        } catch (SQLException e) {
            log.error("Не удалось изменить статус книги {}.", isbn, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int getAvailableCount(String isbn) {
        return getCount("book.availableCount", isbn);
    }

    @Override
    public int getTotalCount(String isbn) {
        return getCount("book.totalCount", isbn);
    }

    @Override
    public List<Author> getAuthorsByIsbn(String isbn) {
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("book.authorsByIsbn"))) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Author author = new Author();
                    author.setAuthorId(rs.getInt("author_id"));
                    author.setLastName(rs.getString("last_name"));
                    author.setFirstName(rs.getString("first_name"));
                    setStringIfPresent(rs, "patronymic", author::setPatronymic);
                    authors.add(author);
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить авторов для ISBN {}.", isbn, e);
        }
        return authors;
    }

    @Override
    public List<Genre> getGenresByIsbn(String isbn) {
        List<Genre> genres = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get("book.genresByIsbn"))) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Genre genre = new Genre();
                    genre.setGenreId(rs.getInt("genre_id"));
                    genre.setName(rs.getString("name"));
                    genres.add(genre);
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить жанры для ISBN {}.", isbn, e);
        }
        return genres;
    }

    @Override
    public void updateAuthors(String isbn, List<Integer> authorIds) {
        updateManyToMany(isbn, authorIds, "book.deleteAuthors", "book.insertAuthor");
    }

    @Override
    public void updateGenres(String isbn, List<Integer> genreIds) {
        updateManyToMany(isbn, genreIds, "book.deleteGenres", "book.insertGenre");
    }

    private void fillBookStatement(PreparedStatement ps, Book book) throws SQLException {
        ps.setString(1, book.getIsbn());
        ps.setString(2, book.getTitle());
        ps.setObject(3, book.getPublicationYear());
        ps.setObject(4, book.getPageCount());
        ps.setString(5, book.getBbk());
        ps.setString(6, book.getAuthorMark());
        ps.setObject(7, book.getPublisherId());
        ps.setBytes(8, book.getCoverImage());
    }

    private int getCount(String sqlKey, String isbn) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SqlProvider.get(sqlKey))) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить количество книг по ключу {} для ISBN {}.", sqlKey, isbn, e);
        }
        return 0;
    }

    private void updateManyToMany(String isbn, List<Integer> ids, String deleteKey, String insertKey) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(SqlProvider.get(deleteKey))) {
                ps.setString(1, isbn);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(SqlProvider.get(insertKey))) {
                for (Integer id : ids) {
                    ps.setString(1, isbn);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    log.warn("Не удалось выполнить откат транзакции.", rollbackError);
                }
            }
            log.error("Не удалось обновить связи книги для ISBN {}.", isbn, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Не удалось закрыть соединение с БД.", e);
                }
            }
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        setIntegerIfPresent(rs, "publication_year", book::setPublicationYear);
        setStringIfPresent(rs, "publisher_name", book::setPublisherName);
        setStringIfPresent(rs, "authors", book::setAuthors);
        setStringIfPresent(rs, "genres", book::setGenres);
        setBooleanIfPresent(rs, "is_active", book::setActive);
        setIntegerIfPresent(rs, "page_count", book::setPageCount);
        setIntegerIfPresent(rs, "total_copies", book::setTotalCopies);
        setIntegerIfPresent(rs, "available_copies", book::setAvailableCopies);
        setStringIfPresent(rs, "bbk", book::setBbk);
        setStringIfPresent(rs, "author_mark", book::setAuthorMark);
        setIntegerIfPresent(rs, "publisher_id", book::setPublisherId);
        setBytesIfPresent(rs, "cover_image", book::setCoverImage);
        return book;
    }

    private interface IntegerSetter {
        void set(Integer value);
    }

    private interface StringSetter {
        void set(String value);
    }

    private interface BooleanSetter {
        void set(Boolean value);
    }

    private interface BytesSetter {
        void set(byte[] value);
    }

    private void setIntegerIfPresent(ResultSet rs, String column, IntegerSetter setter) throws SQLException {
        if (hasColumn(rs, column)) {
            int value = rs.getInt(column);
            if (!rs.wasNull()) {
                setter.set(value);
            }
        }
    }

    private void setStringIfPresent(ResultSet rs, String column, StringSetter setter) throws SQLException {
        if (hasColumn(rs, column)) {
            setter.set(rs.getString(column));
        }
    }

    private void setBooleanIfPresent(ResultSet rs, String column, BooleanSetter setter) throws SQLException {
        if (hasColumn(rs, column)) {
            boolean value = rs.getBoolean(column);
            if (!rs.wasNull()) {
                setter.set(value);
            }
        }
    }

    private void setBytesIfPresent(ResultSet rs, String column, BytesSetter setter) throws SQLException {
        if (hasColumn(rs, column)) {
            setter.set(rs.getBytes(column));
        }
    }

    private boolean hasColumn(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
