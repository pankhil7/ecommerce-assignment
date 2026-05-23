package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.repository.impl.InMemoryCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(new InMemoryCartRepository());
    }

    @Test
    void addItem_newCart_createsCartWithItem() {
        CartItem item = new CartItem("p1", "Widget", 10.0, 2);
        Cart cart = cartService.addItem("user1", "US", item);

        assertEquals(1, cart.getItems().size());
        assertEquals("p1", cart.getItems().get(0).getProductId());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_sameProductTwice_accumulatesQuantity() {
        cartService.addItem("user1", "US", new CartItem("p1", "Widget", 10.0, 2));
        Cart cart = cartService.addItem("user1", "US", new CartItem("p1", "Widget", 10.0, 3));

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_differentProducts_addsMultipleItems() {
        cartService.addItem("user1", "US", new CartItem("p1", "Widget", 10.0, 1));
        Cart cart = cartService.addItem("user1", "US", new CartItem("p2", "Gadget", 20.0, 1));

        assertEquals(2, cart.getItems().size());
    }

    @Test
    void getCart_nonExistentUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cartService.getCart("ghost"));
    }

    @Test
    void clearCart_removesCart() {
        cartService.addItem("user1", "US", new CartItem("p1", "Widget", 10.0, 1));
        cartService.clearCart("user1");

        assertThrows(IllegalArgumentException.class, () -> cartService.getCart("user1"));
    }
}
