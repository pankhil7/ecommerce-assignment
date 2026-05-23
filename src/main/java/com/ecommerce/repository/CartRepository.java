package com.ecommerce.repository;

import com.ecommerce.model.Cart;
import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findByUserId(String userId);
    void deleteByUserId(String userId);
}
