package com.library.dao;

import com.library.model.Book;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT b.*, c.name as category_name
            FROM books b LEFT JOIN categories c ON b.category_id = c.id
            ORDER BY b.id DESC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public List<Book> search(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT b.*, c.name as category_name
            FROM books b LEFT JOIN categories c ON b.category_id = c.id
            WHERE b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ?
            ORDER BY b.id DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public Book getById(int id) {
        String sql = """
            SELECT b.*, c.name as category_name
            FROM books b LEFT JOIN categories c ON b.category_id = c.id
            WHERE b.id = ?
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Book book) {
        String sql = """
            INSERT INTO books (isbn, title, author, publisher, year, category_id, quantity, available, description, cover_image, content)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getYear());
            ps.setInt(6, book.getCategoryId());
            ps.setInt(7, book.getQuantity());
            ps.setInt(8, book.getAvailable());
            ps.setString(9, book.getDescription());
            ps.setString(10, book.getCoverImage());
            ps.setString(11, book.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Book book) {
        String sql = """
            UPDATE books SET isbn=?, title=?, author=?, publisher=?, year=?,
            category_id=?, quantity=?, available=?, description=?, cover_image=?, content=?
            WHERE id=?
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getYear());
            ps.setInt(6, book.getCategoryId());
            ps.setInt(7, book.getQuantity());
            ps.setInt(8, book.getAvailable());
            ps.setString(9, book.getDescription());
            ps.setString(10, book.getCoverImage());
            ps.setString(11, book.getContent());
            ps.setInt(12, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement("DELETE FROM books WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getTotalCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalAvailable() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(available), 0) FROM books")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void decreaseAvailable(int bookId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE books SET available = available - 1 WHERE id = ? AND available > 0")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void increaseAvailable(int bookId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE books SET available = available + 1 WHERE id = ? AND available < quantity")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void decreaseQuantity(int bookId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE books SET quantity = quantity - 1 WHERE id = ? AND quantity > 0")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Book mapResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setYear(rs.getInt("year"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setQuantity(rs.getInt("quantity"));
        book.setAvailable(rs.getInt("available"));
        book.setDescription(rs.getString("description"));
        book.setCoverImage(rs.getString("cover_image"));
        try { book.setContent(rs.getString("content")); } catch (SQLException ignored) {}
        try { book.setCategoryName(rs.getString("category_name")); } catch (SQLException ignored) {}
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { book.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T"))); } catch (Exception ignored) {}
        }
        return book;
    }
}
