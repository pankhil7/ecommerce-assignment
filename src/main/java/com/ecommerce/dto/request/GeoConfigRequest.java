package com.ecommerce.dto.request;

public record GeoConfigRequest(
        String geoId,
        int nthOrder,
        double discountPercentage
) {}
