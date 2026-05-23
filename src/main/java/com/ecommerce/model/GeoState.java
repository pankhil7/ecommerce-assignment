package com.ecommerce.model;

public class GeoState {
    private String geoId;
    private int orderCount;
    private boolean pendingCodeGeneration;
    private String eligibleUserId;  // user who placed the nth order

    public GeoState(String geoId, int orderCount, boolean pendingCodeGeneration, String eligibleUserId) {
        this.geoId = geoId;
        this.orderCount = orderCount;
        this.pendingCodeGeneration = pendingCodeGeneration;
        this.eligibleUserId = eligibleUserId;
    }

    public String getGeoId() { return geoId; }
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    public boolean isPendingCodeGeneration() { return pendingCodeGeneration; }
    public void setPendingCodeGeneration(boolean pendingCodeGeneration) { this.pendingCodeGeneration = pendingCodeGeneration; }
    public String getEligibleUserId() { return eligibleUserId; }
    public void setEligibleUserId(String eligibleUserId) { this.eligibleUserId = eligibleUserId; }
}
