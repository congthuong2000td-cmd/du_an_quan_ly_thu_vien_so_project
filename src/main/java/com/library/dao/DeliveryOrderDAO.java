package com.library.dao;

import com.library.model.DeliveryOrder;
import com.library.model.Book;
import com.library.model.Reader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeliveryOrderDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private final BookDAO bookDAO = new BookDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();

    public List<DeliveryOrder> getAll() {
        List<DeliveryOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM delivery_orders ORDER BY created_at DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public List<DeliveryOrder> getByReaderId(int readerId) {
        List<DeliveryOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM delivery_orders WHERE reader_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, readerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public DeliveryOrder getById(int id) {
        String sql = "SELECT * FROM delivery_orders WHERE id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(DeliveryOrder order, List<Integer> bookIds) {
        String insertOrderSql = """
            INSERT INTO delivery_orders 
            (reader_id, type, recipient_name, recipient_phone, delivery_address, 
            shipping_fee, deposit_fee, total_amount, payment_method, payment_status, status, note) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        String insertItemSql = "INSERT INTO delivery_order_items (order_id, book_id) VALUES (?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // start transaction
            
            // Insert Order
            int orderId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getReaderId());
                ps.setString(2, order.getType());
                ps.setString(3, order.getRecipientName());
                ps.setString(4, order.getRecipientPhone());
                ps.setString(5, order.getDeliveryAddress());
                ps.setDouble(6, order.getShippingFee());
                ps.setDouble(7, order.getDepositFee());
                ps.setDouble(8, order.getTotalAmount());
                ps.setString(9, order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
                ps.setString(10, order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING");
                ps.setString(11, order.getStatus());
                ps.setString(12, order.getNote());
                
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    orderId = rs.getInt(1);
                    order.setId(orderId);
                }
            }
            
            if (orderId != -1 && bookIds != null) {
                try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                    for (Integer bookId : bookIds) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, bookId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) { 
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace(); 
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        return false;
    }

    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE delivery_orders SET status=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updatePaymentStatus(int orderId, String paymentStatus) {
        String sql = "UPDATE delivery_orders SET payment_status=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private DeliveryOrder mapResultSet(ResultSet rs) throws SQLException {
        DeliveryOrder order = new DeliveryOrder();
        order.setId(rs.getInt("id"));
        order.setReaderId(rs.getInt("reader_id"));
        order.setType(rs.getString("type"));
        order.setRecipientName(rs.getString("recipient_name"));
        order.setRecipientPhone(rs.getString("recipient_phone"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setShippingFee(rs.getDouble("shipping_fee"));
        order.setDepositFee(rs.getDouble("deposit_fee"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setStatus(rs.getString("status"));
        order.setNote(rs.getString("note"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { order.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T").split("\\.")[0])); } catch (Exception ignored) {}
        }
        
        // Fetch relations lazily or actively (Here we fetch actively for simplicity)
        Reader reader = readerDAO.getById(order.getReaderId());
        order.setReader(reader);
        
        // Fetch order items
        List<Book> books = new ArrayList<>();
        String itemSql = "SELECT book_id FROM delivery_order_items WHERE order_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(itemSql)) {
            ps.setInt(1, order.getId());
            ResultSet rsItems = ps.executeQuery();
            while (rsItems.next()) {
                Book b = bookDAO.getById(rsItems.getInt("book_id"));
                if (b != null) books.add(b);
            }
        }
        order.setBooks(books);
        
        return order;
    }
}
