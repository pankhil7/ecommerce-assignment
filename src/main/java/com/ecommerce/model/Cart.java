package com.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String userId;
    private String geoId;
    private List<CartItem> items;

    public Cart(String userId, String geoId) {
        this.userId = userId;
        this.geoId = geoId;
        this.items = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public String getGeoId() { return geoId; }
    public List<CartItem> getItems() { return items; }
}
