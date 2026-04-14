package com.library.dao;

import com.library.model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public boolean insert(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, book_id, status, request_date, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, reservation.getUserId());
            ps.setInt(2, reservation.getBookId());
            ps.setString(3, reservation.getStatus());
            ps.setString(4, reservation.getRequestDate().toString());
            ps.setString(5, reservation.getNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Reservation> getByUserId(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.title as book_title, b.author as book_author
            FROM reservations r
            JOIN books b ON r.book_id = b.id
            WHERE r.user_id = ?
            ORDER BY r.request_date DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> getAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.title as book_title, b.author as book_author, u.full_name as user_name
            FROM reservations r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.user_id = u.id
            ORDER BY CASE r.status WHEN 'PENDING' THEN 0 ELSE 1 END, r.request_date DESC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> getPending() {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.title as book_title, b.author as book_author, u.full_name as user_name
            FROM reservations r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.user_id = u.id
            WHERE r.status = 'PENDING'
            ORDER BY r.request_date ASC
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getPendingCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reservations WHERE status='PENDING'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean approve(int reservationId) {
        return updateStatus(reservationId, "APPROVED");
    }

    public boolean reject(int reservationId) {
        return updateStatus(reservationId, "REJECTED");
    }

    public boolean cancel(int reservationId) {
        return updateStatus(reservationId, "CANCELLED");
    }

    private boolean updateStatus(int id, String status) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE reservations SET status=?, response_date=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean hasPendingReservation(int userId, int bookId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM reservations WHERE user_id=? AND book_id=? AND status='PENDING'")) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Reservation mapResultSet(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setStatus(rs.getString("status"));
        String reqDate = rs.getString("request_date");
        if (reqDate != null) r.setRequestDate(LocalDate.parse(reqDate));
        String resDate = rs.getString("response_date");
        if (resDate != null) r.setResponseDate(LocalDate.parse(resDate));
        r.setNote(rs.getString("note"));
        try { r.setBookTitle(rs.getString("book_title")); } catch (SQLException ignored) {}
        try { r.setBookAuthor(rs.getString("book_author")); } catch (SQLException ignored) {}
        try { r.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        return r;
    }
}
