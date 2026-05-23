package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String geoId;
    private List<OrderItem> items;
    private double subtotal;
    private String discountCode;
    private double discountAmount;
    private double total;
    private LocalDateTime createdAt;

    private Order() {}

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getGeoId() { return geoId; }
    public List<OrderItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public String getDiscountCode() { return discountCode; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Builder pattern
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Order order = new Order();

        public Builder orderId(String orderId) { order.orderId = orderId; return this; }
        public Builder userId(String userId) { order.userId = userId; return this; }
        public Builder geoId(String geoId) { order.geoId = geoId; return this; }
        public Builder items(List<OrderItem> items) { order.items = items; return this; }
        public Builder subtotal(double subtotal) { order.subtotal = subtotal; return this; }
        public Builder discountCode(String discountCode) { order.discountCode = discountCode; return this; }
        public Builder discountAmount(double discountAmount) { order.discountAmount = discountAmount; return this; }
        public Builder total(double total) { order.total = total; return this; }
        public Builder createdAt(LocalDateTime createdAt) { order.createdAt = createdAt; return this; }
        public Order build() { return order; }
    }
}
