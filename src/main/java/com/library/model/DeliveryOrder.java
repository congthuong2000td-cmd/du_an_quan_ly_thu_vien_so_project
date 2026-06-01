package com.library.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeliveryOrder {
    private int id;
    private int readerId;
    private String type; // DELIVERY, PICKUP
    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private double shippingFee;
    private double depositFee;
    private double totalAmount;
    private String paymentMethod; // COD, BANK_TRANSFER
    private String paymentStatus; // PENDING, PAID
    private String status; // PENDING, PREPARING, READY_FOR_DELIVERY, DELIVERING, DELIVERED, CANCELED, FAILED
    private String note;
    private LocalDateTime createdAt;
    
    private Reader reader;
    private List<Book> books = new ArrayList<>();

    public DeliveryOrder() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReaderId() { return readerId; }
    public void setReaderId(int readerId) { this.readerId = readerId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    public double getDepositFee() { return depositFee; }
    public void setDepositFee(double depositFee) { this.depositFee = depositFee; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }
    public List<Book> getBooks() { return books; }
    public void setBooks(List<Book> books) { this.books = books; }
    public void addBook(Book book) { this.books.add(book); }
}
