package com.library.model;

import java.time.LocalDate;

public class BorrowRecord {
    private int id;
    private int readerId;
    private int bookId;
    private String readerName;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;
    private String status;

    public BorrowRecord() {}

    public BorrowRecord(int readerId, int bookId, LocalDate borrowDate,
                        LocalDate dueDate) {
        this.readerId = readerId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = "BORROWING";
        this.fine = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReaderId() { return readerId; }
    public void setReaderId(int readerId) { this.readerId = readerId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDisplay() {
        return switch (status) {
            case "BORROWING" -> "Đang mượn";
            case "RETURNED" -> "Đã trả";
            case "OVERDUE" -> "Quá hạn";
            case "LOST" -> "Mất sách";
            default -> status;
        };
    }
}
