package com.ecommerce.dto.response;

public record CheckoutResponse(
        String orderId,
        double subtotal,
        double discountAmount,
        double total,
        boolean nthOrderReached   // hint to admin that a discount code can now be generated
) {}
