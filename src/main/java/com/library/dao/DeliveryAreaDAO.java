package com.library.dao;

import com.library.model.DeliveryArea;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryAreaDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<DeliveryArea> getAllActive() {
        List<DeliveryArea> areas = new ArrayList<>();
        String sql = "SELECT * FROM delivery_areas WHERE is_active = 1 ORDER BY district, ward";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                areas.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return areas;
    }
    
    public List<DeliveryArea> getAll() {
        List<DeliveryArea> areas = new ArrayList<>();
        String sql = "SELECT * FROM delivery_areas ORDER BY district, ward";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                areas.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return areas;
    }

    public DeliveryArea getById(int id) {
        String sql = "SELECT * FROM delivery_areas WHERE id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(DeliveryArea area) {
        String sql = "INSERT INTO delivery_areas (district, ward, base_fee, is_active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, area.getDistrict());
            ps.setString(2, area.getWard());
            ps.setDouble(3, area.getBaseFee());
            ps.setInt(4, area.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(DeliveryArea area) {
        String sql = "UPDATE delivery_areas SET district=?, ward=?, base_fee=?, is_active=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, area.getDistrict());
            ps.setString(2, area.getWard());
            ps.setDouble(3, area.getBaseFee());
            ps.setInt(4, area.isActive() ? 1 : 0);
            ps.setInt(5, area.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private DeliveryArea mapResultSet(ResultSet rs) throws SQLException {
        DeliveryArea area = new DeliveryArea();
        area.setId(rs.getInt("id"));
        area.setDistrict(rs.getString("district"));
        area.setWard(rs.getString("ward"));
        area.setBaseFee(rs.getDouble("base_fee"));
        area.setActive(rs.getInt("is_active") == 1);
        return area;
    }
}
