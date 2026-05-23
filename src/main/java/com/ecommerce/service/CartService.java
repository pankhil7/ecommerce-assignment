package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart addItem(String userId, String geoId, CartItem item) {
        log.info("Adding item [productId={}] to cart [userId={}, geoId={}]", item.getProductId(), userId, geoId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.debug("No existing cart found — creating new cart for user [{}]", userId);
                    return new Cart(userId, geoId);
                });

        // If the product already exists in the cart, increment quantity
        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            log.debug("Product [{}] already in cart — updating quantity from {} to {}",
                                    item.getProductId(), existing.getQuantity(), existing.getQuantity() + item.getQuantity());
                            existing.setQuantity(existing.getQuantity() + item.getQuantity());
                        },
                        () -> cart.getItems().add(item)
                );

        return cartRepository.save(cart);
    }

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Cart lookup failed — no cart found for user [{}]", userId);
                    return new IllegalArgumentException("Cart not found for user: " + userId);
                });
    }

    public void clearCart(String userId) {
        log.info("Clearing cart for user [{}]", userId);
        cartRepository.deleteByUserId(userId);
    }
}
