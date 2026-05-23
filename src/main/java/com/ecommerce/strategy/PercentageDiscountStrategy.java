package com.ecommerce.strategy;

import org.springframework.stereotype.Component;

// Strategy pattern: concrete implementation of percentage-based discounts.
// New discount types (flat amount, tiered) can be added by implementing DiscountStrategy.
@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculate(double subtotal, double percentage) {
        return subtotal * (percentage / 100.0);
    }
}
