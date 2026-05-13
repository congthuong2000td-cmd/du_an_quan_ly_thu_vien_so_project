package com.library.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private int id;
    private String name;
    private String type; // DIRECT, GROUP
    private LocalDateTime createdAt;
    
    // Extra fields for UI
    private List<User> members = new ArrayList<>();
    private Message lastMessage;
    private int unreadCount;

    public Conversation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }

    public Message getLastMessage() { return lastMessage; }
    public void setLastMessage(Message lastMessage) { this.lastMessage = lastMessage; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    
    public String getDisplayName(int currentUserId) {
        if ("DIRECT".equals(type)) {
            for (User member : members) {
                if (member.getId() != currentUserId) {
                    return member.getFullName();
                }
            }
        }
        return name != null ? name : "Cuộc trò chuyện";
    }
}
