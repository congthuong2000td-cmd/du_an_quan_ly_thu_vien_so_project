package com.library.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.model.User;
import com.library.util.ValidationUtils;

public class UserDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    /**
     * Authenticate user - only active accounts can login
     * Returns null if credentials wrong OR account not active
     * Returns special "INACTIVE" user if correct credentials but inactive
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, ValidationUtils.hashPassword(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY active ASC, id DESC")) {
            while (rs.next()) users.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "SELECT * FROM users WHERE active=0 ORDER BY id DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public int getPendingCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE active=0")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, active, security_question, security_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, ValidationUtils.hashPassword(user.getPassword()));
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.setString(6, user.getSecurityQuestion());
            ps.setString(7, user.getSecurityAnswer());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean usernameExists(String username) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean activateUser(int userId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE users SET active=1 WHERE id=?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deactivateUser(int userId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE users SET active=0 WHERE id=?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteUser(int userId) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "DELETE FROM users WHERE id=?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean changePassword(int userId, String newPassword) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE users SET password=? WHERE id=?")) {
            ps.setString(1, ValidationUtils.hashPassword(newPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateFullName(int userId, String newFullName) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE users SET full_name=? WHERE id=?")) {
            ps.setString(1, newFullName);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateRole(int userId, String newRole) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE users SET role=? WHERE id=?")) {
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getInt("active") == 1);
        user.setSecurityQuestion(rs.getString("security_question"));
        user.setSecurityAnswer(rs.getString("security_answer"));
        user.setStatus(rs.getString("status"));
        String lastSeen = rs.getString("last_seen");
        if (lastSeen != null && !lastSeen.isEmpty()) {
            try {
                // SQL Server returns datetime with milliseconds like "2026-05-19 00:56:27.0"
                // Take only the first 19 characters to match pattern "yyyy-MM-dd HH:mm:ss"
                String cleanedLastSeen = lastSeen.split("\\.")[0];
                user.setLastSeen(java.time.LocalDateTime.parse(cleanedLastSeen, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                // If parsing fails, just skip setting lastSeen
                System.err.println("Error parsing lastSeen: " + lastSeen + " - " + e.getMessage());
            }
        }
        return user;
    }

    public boolean updateStatus(int userId, String status) {
        String sql = "UPDATE users SET status=?, last_seen=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<User> searchUsers(String query, int excludeUserId) {
        List<User> users = new ArrayList<>();
        String limitClause = com.library.util.Constants.DB_URL.startsWith("jdbc:sqlite") 
            ? " LIMIT 20" : " ORDER BY id OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY";
        String sql = "SELECT * FROM users WHERE (username LIKE ? OR full_name LIKE ?) AND id != ? AND active = 1" + limitClause;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setInt(3, excludeUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean resetPasswordByUsername(String username, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE username=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, ValidationUtils.hashPassword(newPassword));
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
