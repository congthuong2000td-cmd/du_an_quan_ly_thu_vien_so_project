package com.library.dao;

import com.library.model.Favorite;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public boolean toggleFavorite(int userId, int bookId) {
        if (isFavorite(userId, bookId)) {
            return removeFavorite(userId, bookId);
        } else {
            return addFavorite(userId, bookId);
        }
    }

    private boolean addFavorite(int userId, int bookId) {
        String sql = "INSERT INTO favorites (user_id, book_id) VALUES (?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean removeFavorite(int userId, int bookId) {
        String sql = "DELETE FROM favorites WHERE user_id=? AND book_id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean isFavorite(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id=? AND book_id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Favorite> getByUser(int userId) {
        List<Favorite> list = new ArrayList<>();
        String sql = """
            SELECT f.*, b.title as book_title, b.author as book_author, b.category as book_category
            FROM favorites f
            JOIN books b ON f.book_id = b.id
            WHERE f.user_id = ?
            ORDER BY f.created_at DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Favorite f = new Favorite();
                f.setUserId(rs.getInt("user_id"));
                f.setBookId(rs.getInt("book_id"));
                String dateStr = rs.getString("created_at");
                if (dateStr != null) {
                    f.setCreatedAt(LocalDateTime.parse(dateStr.replace(" ", "T")));
                }
                f.setBookTitle(rs.getString("book_title"));
                f.setBookAuthor(rs.getString("book_author"));
                f.setBookCategory(rs.getString("book_category"));
                list.add(f);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
