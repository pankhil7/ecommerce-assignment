package com.ecommerce.model;

import java.util.List;

public record AnalyticsResult(
        int totalOrders,
        double totalRevenue,
        double totalDiscountAmount,
        List<DiscountCode> discountCodes
) {}
