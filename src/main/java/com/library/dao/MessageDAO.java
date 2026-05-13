package com.library.dao;

import com.library.model.Message;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addMessage(Message message) {
        String sql = "INSERT INTO messages (conversation_id, sender_id, content, type, file_path, sent_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getConversationId());
            ps.setInt(2, message.getSenderId());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getType());
            ps.setString(5, message.getFilePath());
            ps.setString(6, LocalDateTime.now().format(formatter));
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        message.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Message> getMessagesByConversation(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name as sender_name FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE conversation_id = ? AND is_deleted = 0 " +
                     "ORDER BY sent_at ASC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public boolean markAsSeen(int conversationId, int currentUserId) {
        String sql = "UPDATE messages SET is_seen = 1 WHERE conversation_id = ? AND sender_id != ? AND is_seen = 0";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, currentUserId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteMessage(int messageId) {
        String sql = "UPDATE messages SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setConversationId(rs.getInt("conversation_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setContent(rs.getString("content"));
        m.setType(rs.getString("type"));
        m.setFilePath(rs.getString("file_path"));
        m.setSentAt(LocalDateTime.parse(rs.getString("sent_at"), formatter));
        m.setSeen(rs.getInt("is_seen") == 1);
        m.setDeleted(rs.getInt("is_deleted") == 1);
        m.setSenderName(rs.getString("sender_name"));
        return m;
    }
}
