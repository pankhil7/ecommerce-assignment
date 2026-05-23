package com.ecommerce.dto.request;

public record AddToCartRequest(
        String userId,
        String geoId,
        String productId,
        String name,
        double price,
        int quantity
) {}
