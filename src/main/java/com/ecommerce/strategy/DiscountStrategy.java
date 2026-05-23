package com.ecommerce.strategy;

public interface DiscountStrategy {
    double calculate(double subtotal, double percentage);
}
