package com.ecommerce.model;

import java.time.LocalDateTime;

public class DiscountCode {
    private String code;
    private String geoId;
    private String userId;      // only this user can redeem it
    private double percentage;
    private boolean used;
    private LocalDateTime createdAt;

    public DiscountCode(String code, String geoId, String userId, double percentage,
                        boolean used, LocalDateTime createdAt) {
        this.code = code;
        this.geoId = geoId;
        this.userId = userId;
        this.percentage = percentage;
        this.used = used;
        this.createdAt = createdAt;
    }

    public String getCode() { return code; }
    public String getGeoId() { return geoId; }
    public String getUserId() { return userId; }
    public double getPercentage() { return percentage; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
