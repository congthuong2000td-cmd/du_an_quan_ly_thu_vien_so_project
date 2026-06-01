package com.library.dao;

import com.library.model.DeliveryTask;
import com.library.model.DeliveryOrder;
import com.library.model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeliveryTaskDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private final DeliveryOrderDAO orderDAO = new DeliveryOrderDAO();
    private final UserDAO userDAO = new UserDAO();

    public List<DeliveryTask> getAll() {
        List<DeliveryTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM delivery_tasks ORDER BY created_at DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }
    
    public List<DeliveryTask> getTasksByShipperId(int shipperId) {
        List<DeliveryTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM delivery_tasks WHERE shipper_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, shipperId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }

    public boolean insert(DeliveryTask task) {
        String sql = "INSERT INTO delivery_tasks (order_id, shipper_id, shipper_type, external_provider, tracking_code, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getOrderId());
            if (task.getShipperId() != null) {
                ps.setInt(2, task.getShipperId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, task.getShipperType() != null ? task.getShipperType() : "INTERNAL");
            ps.setString(4, task.getExternalProvider());
            ps.setString(5, task.getTrackingCode());
            ps.setString(6, task.getStatus() != null ? task.getStatus() : "ASSIGNED");
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateTaskStatus(int taskId, String status, String proofImageUrl, String failureReason) {
        String sql = "UPDATE delivery_tasks SET status=?, proof_image_url=?, failure_reason=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, proofImageUrl);
            ps.setString(3, failureReason);
            ps.setInt(4, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public boolean assignShipper(int taskId, int shipperId) {
        String sql = "UPDATE delivery_tasks SET shipper_id=?, shipper_type='INTERNAL', external_provider=NULL, tracking_code=NULL, status='ASSIGNED', updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, shipperId);
            ps.setInt(2, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    public boolean assignExternalShipper(int taskId, String provider, String trackingCode) {
        String sql = "UPDATE delivery_tasks SET shipper_id=NULL, shipper_type='EXTERNAL', external_provider=?, tracking_code=?, status='ASSIGNED', updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, provider);
            ps.setString(2, trackingCode);
            ps.setInt(3, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private DeliveryTask mapResultSet(ResultSet rs) throws SQLException {
        DeliveryTask task = new DeliveryTask();
        task.setId(rs.getInt("id"));
        task.setOrderId(rs.getInt("order_id"));
        
        int shipperId = rs.getInt("shipper_id");
        if (!rs.wasNull()) {
            task.setShipperId(shipperId);
        }
        
        task.setShipperType(rs.getString("shipper_type"));
        task.setExternalProvider(rs.getString("external_provider"));
        task.setTrackingCode(rs.getString("tracking_code"));
        
        task.setStatus(rs.getString("status"));
        task.setProofImageUrl(rs.getString("proof_image_url"));
        task.setFailureReason(rs.getString("failure_reason"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { task.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T").split("\\.")[0])); } catch (Exception ignored) {}
        }
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) {
            try { task.setUpdatedAt(LocalDateTime.parse(updatedAt.replace(" ", "T").split("\\.")[0])); } catch (Exception ignored) {}
        }
        
        // Fetch order
        DeliveryOrder order = orderDAO.getById(task.getOrderId());
        task.setOrder(order);
        
        // Fetch shipper if exists
        if (task.getShipperId() != null) {
            User shipper = userDAO.getById(task.getShipperId());
            task.setShipper(shipper);
        }
        
        return task;
    }
}
