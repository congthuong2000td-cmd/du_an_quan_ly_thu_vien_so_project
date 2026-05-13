package com.library.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean active;
    private String securityQuestion;
    private String securityAnswer;
    private String status; // ONLINE, OFFLINE
    private java.time.LocalDateTime lastSeen;

    public User() {}

    public User(String username, String password, String fullName, String role, String securityQuestion, String securityAnswer) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = false; // default: chờ duyệt
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public String getStatusText() {
        return active ? "Đã duyệt" : "Chờ duyệt";
    }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(java.time.LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
