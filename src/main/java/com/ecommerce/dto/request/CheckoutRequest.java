package com.ecommerce.dto.request;

public record CheckoutRequest(
        String userId,
        String geoId,
        String discountCode   // nullable — checkout without code is valid
) {}
