package com.library.dao;

import com.library.model.Conversation;
import com.library.model.Message;
import com.library.model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public int createConversation(String name, String type, List<Integer> userIds) {
        String sqlConv = "INSERT INTO conversations (name, type) VALUES (?, ?)";
        String sqlMember = "INSERT INTO conversation_members (conversation_id, user_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int convId;
                try (PreparedStatement ps = conn.prepareStatement(sqlConv, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, type);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) convId = rs.getInt(1);
                        else throw new SQLException("Failed to create conversation");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlMember)) {
                    for (Integer userId : userIds) {
                        ps.setInt(1, convId);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return convId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Conversation> getUserConversations(int userId) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT c.* FROM conversations c " +
                     "JOIN conversation_members cm ON c.id = cm.conversation_id " +
                     "WHERE cm.user_id = ? " +
                     "ORDER BY c.created_at DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Conversation conv = mapResultSetToConversation(rs);
                    conv.setMembers(getConversationMembers(conv.getId()));
                    conv.setLastMessage(getLastMessage(conv.getId()));
                    conv.setUnreadCount(getUnreadCount(conv.getId(), userId));
                    conversations.add(conv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public List<User> getConversationMembers(int conversationId) {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "JOIN conversation_members cm ON u.id = cm.user_id " +
                     "WHERE cm.conversation_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    members.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public int findDirectConversation(int user1, int user2) {
        String sql = "SELECT cm1.conversation_id FROM conversation_members cm1 " +
                     "JOIN conversation_members cm2 ON cm1.conversation_id = cm2.conversation_id " +
                     "JOIN conversations c ON cm1.conversation_id = c.id " +
                     "WHERE cm1.user_id = ? AND cm2.user_id = ? AND c.type = 'DIRECT'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user1);
            ps.setInt(2, user2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private Message getLastMessage(int conversationId) {
        String sql = "SELECT m.*, u.full_name as sender_name FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE conversation_id = ? AND is_deleted = 0 " +
                     "ORDER BY sent_at DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("id"));
                    m.setContent(rs.getString("content"));
                    m.setSentAt(LocalDateTime.parse(rs.getString("sent_at"), formatter));
                    m.setSenderName(rs.getString("sender_name"));
                    return m;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getUnreadCount(int conversationId, int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE conversation_id = ? AND sender_id != ? AND is_seen = 0";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation c = new Conversation();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setType(rs.getString("type"));
        c.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), formatter));
        return c;
    }
}
