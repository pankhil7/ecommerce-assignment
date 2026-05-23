package com.ecommerce.factory;

import com.ecommerce.model.DiscountCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

// Factory pattern: centralises discount code creation.
// Code format: 12-char alphanumeric UUID slice — collision-free, non-guessable.
@Component
public class DiscountCodeFactory {

    public DiscountCode create(String geoId, String userId, double percentage) {
        String code = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return new DiscountCode(code, geoId, userId, percentage, false, LocalDateTime.now());
    }
}
