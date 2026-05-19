package com.library.dao;

import com.library.model.Reader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Reader> getAll() {
        List<Reader> readers = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM readers ORDER BY id DESC")) {
            while (rs.next()) readers.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return readers;
    }

    public List<Reader> search(String keyword) {
        List<Reader> readers = new ArrayList<>();
        String sql = "SELECT * FROM readers WHERE name LIKE ? OR code LIKE ? OR phone LIKE ? ORDER BY id DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) readers.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return readers;
    }

    public Reader getById(int id) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement("SELECT * FROM readers WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Reader getByName(String name) {
        String limitClause = com.library.util.Constants.DB_URL.startsWith("jdbc:sqlite") 
            ? " LIMIT 1" : " ORDER BY id OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY";
        String sql = "SELECT * FROM readers WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))" + limitClause;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Reader reader) {
        String sql = "INSERT INTO readers (code, name, email, phone, address, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, reader.getCode());
            ps.setString(2, reader.getName());
            ps.setString(3, reader.getEmail());
            ps.setString(4, reader.getPhone());
            ps.setString(5, reader.getAddress());
            ps.setString(6, reader.getType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Reader reader) {
        String sql = "UPDATE readers SET code=?, name=?, email=?, phone=?, address=?, type=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, reader.getCode());
            ps.setString(2, reader.getName());
            ps.setString(3, reader.getEmail());
            ps.setString(4, reader.getPhone());
            ps.setString(5, reader.getAddress());
            ps.setString(6, reader.getType());
            ps.setInt(7, reader.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement("DELETE FROM readers WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getTotalCount() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM readers")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public String generateNextCode() {
        String sql = com.library.util.Constants.DB_URL.startsWith("jdbc:sqlite")
            ? "SELECT MAX(CAST(SUBSTR(code, 3) AS INTEGER)) FROM readers WHERE code LIKE 'DG%'"
            : "SELECT MAX(CAST(SUBSTRING(code, 3, LEN(code)) AS INT)) FROM readers WHERE code LIKE 'DG%'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int next = rs.getInt(1) + 1;
                return String.format("DG%03d", next);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "DG001";
    }

    private Reader mapResultSet(ResultSet rs) throws SQLException {
        Reader reader = new Reader();
        reader.setId(rs.getInt("id"));
        reader.setCode(rs.getString("code"));
        reader.setName(rs.getString("name"));
        reader.setEmail(rs.getString("email"));
        reader.setPhone(rs.getString("phone"));
        reader.setAddress(rs.getString("address"));
        reader.setType(rs.getString("type"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { reader.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T"))); } catch (Exception ignored) {}
        }
        return reader;
    }
}
