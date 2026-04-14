package com.library.model;

import java.time.LocalDate;

public class Reservation {
    private int id;
    private int userId;
    private int bookId;
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED
    private LocalDate requestDate;
    private LocalDate responseDate;
    private String note;

    // Joined fields
    private String bookTitle;
    private String bookAuthor;
    private String userName;

    public Reservation() {}

    public Reservation(int userId, int bookId) {
        this.userId = userId;
        this.bookId = bookId;
        this.status = "PENDING";
        this.requestDate = LocalDate.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public LocalDate getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDate responseDate) { this.responseDate = responseDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatusText() {
        return switch (status) {
            case "PENDING" -> "⏳ Chờ duyệt";
            case "APPROVED" -> "✅ Đã duyệt";
            case "REJECTED" -> "❌ Từ chối";
            case "CANCELLED" -> "🚫 Đã hủy";
            default -> status;
        };
    }
}
