package com.library.model;

import java.time.LocalDateTime;

public class DeliveryTask {
    private int id;
    private int orderId;
    private Integer shipperId; // can be null if not assigned
    private String shipperType; // INTERNAL, EXTERNAL
    private String externalProvider; // Grab, AhaMove
    private String trackingCode;
    private String status; // ASSIGNED, IN_TRANSIT, SUCCESS, FAILED
    private String proofImageUrl;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private DeliveryOrder order;
    private User shipper;

    public DeliveryTask() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public Integer getShipperId() { return shipperId; }
    public void setShipperId(Integer shipperId) { this.shipperId = shipperId; }
    public String getShipperType() { return shipperType; }
    public void setShipperType(String shipperType) { this.shipperType = shipperType; }
    public String getExternalProvider() { return externalProvider; }
    public void setExternalProvider(String externalProvider) { this.externalProvider = externalProvider; }
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProofImageUrl() { return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) { this.proofImageUrl = proofImageUrl; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public DeliveryOrder getOrder() { return order; }
    public void setOrder(DeliveryOrder order) { this.order = order; }
    public User getShipper() { return shipper; }
    public void setShipper(User shipper) { this.shipper = shipper; }
}
