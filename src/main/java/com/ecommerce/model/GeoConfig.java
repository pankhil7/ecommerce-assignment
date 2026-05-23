package com.ecommerce.model;

public class GeoConfig {
    private String geoId;
    private int nthOrder;           // every nth order triggers a discount
    private double discountPercentage; // x% off

    public GeoConfig(String geoId, int nthOrder, double discountPercentage) {
        this.geoId = geoId;
        this.nthOrder = nthOrder;
        this.discountPercentage = discountPercentage;
    }

    public String getGeoId() { return geoId; }
    public int getNthOrder() { return nthOrder; }
    public void setNthOrder(int nthOrder) { this.nthOrder = nthOrder; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
}
