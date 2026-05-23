package com.ecommerce.model;

public record CheckoutResult(Order order, boolean nthOrderReached) {}
