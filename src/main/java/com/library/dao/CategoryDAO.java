package com.library.dao;

import com.library.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories ORDER BY name")) {
            while (rs.next()) {
                Category cat = new Category();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                categories.add(cat);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return categories;
    }

    public boolean insert(Category category) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "INSERT INTO categories (name) VALUES (?)")) {
            ps.setString(1, category.getName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Category category) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE categories SET name=? WHERE id=?")) {
            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "DELETE FROM categories WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
