package com.library.model;

public class DeliveryArea {
    private int id;
    private String district;
    private String ward;
    private double baseFee;
    private boolean isActive;

    public DeliveryArea() {
    }

    public DeliveryArea(int id, String district, String ward, double baseFee, boolean isActive) {
        this.id = id;
        this.district = district;
        this.ward = ward;
        this.baseFee = baseFee;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public double getBaseFee() { return baseFee; }
    public void setBaseFee(double baseFee) { this.baseFee = baseFee; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    @Override
    public String toString() {
        return district + " - " + ward;
    }
}
