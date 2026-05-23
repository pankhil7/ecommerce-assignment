package com.ecommerce.repository.impl;

import com.ecommerce.model.Cart;
import com.ecommerce.repository.CartRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCartRepository implements CartRepository {

    private final Map<String, Cart> store = new ConcurrentHashMap<>();

    @Override
    public Cart save(Cart cart) {
        store.put(cart.getUserId(), cart);
        return cart;
    }

    @Override
    public Optional<Cart> findByUserId(String userId) {
        return Optional.ofNullable(store.get(userId));
    }

    @Override
    public void deleteByUserId(String userId) {
        store.remove(userId);
    }
}
