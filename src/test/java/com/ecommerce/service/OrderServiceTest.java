package com.ecommerce.service;

import com.ecommerce.factory.DiscountCodeFactory;
import com.ecommerce.model.*;
import com.ecommerce.repository.impl.*;
import com.ecommerce.strategy.PercentageDiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private CartService cartService;
    private DiscountService discountService;
    private InMemoryGeoRepository geoRepository;
    private InMemoryOrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        InMemoryCartRepository cartRepository = new InMemoryCartRepository();
        InMemoryDiscountRepository discountRepository = new InMemoryDiscountRepository();
        geoRepository = new InMemoryGeoRepository();
        orderRepository = new InMemoryOrderRepository();

        cartService = new CartService(cartRepository);
        discountService = new DiscountService(discountRepository, geoRepository,
                new DiscountCodeFactory(), new PercentageDiscountStrategy());
        orderService = new OrderService(orderRepository, geoRepository, cartService, discountService);

        geoRepository.saveConfig(new GeoConfig("US", 5, 10.0));
    }

    private void addItemToCart(String userId, String productId, double price, int qty) {
        cartService.addItem(userId, "US", new CartItem(productId, "Product " + productId, price, qty));
    }

    @Test
    void checkout_noDiscount_createsOrderAndClearsCart() {
        addItemToCart("user1", "p1", 50.0, 2);

        CheckoutResult result = orderService.checkout("user1", "US", null);

        assertNotNull(result.order().getOrderId());
        assertEquals(100.0, result.order().getSubtotal());
        assertEquals(0.0, result.order().getDiscountAmount());
        assertEquals(100.0, result.order().getTotal());
        assertFalse(result.nthOrderReached());
        assertThrows(IllegalArgumentException.class, () -> cartService.getCart("user1"));
    }

    @Test
    void checkout_nthOrder_signalsNthOrderReached() {
        for (int i = 1; i <= 4; i++) {
            addItemToCart("user" + i, "p1", 10.0, 1);
            orderService.checkout("user" + i, "US", null);
        }

        addItemToCart("user5", "p1", 10.0, 1);
        CheckoutResult result = orderService.checkout("user5", "US", null);

        assertTrue(result.nthOrderReached());
    }

    @Test
    void checkout_withValidDiscount_appliesDiscount() {
        // Place 5 orders to reach nth order
        for (int i = 1; i <= 4; i++) {
            addItemToCart("user" + i, "p1", 10.0, 1);
            orderService.checkout("user" + i, "US", null);
        }
        addItemToCart("user5", "p1", 10.0, 1);
        orderService.checkout("user5", "US", null);

        // Admin generates code for user5
        GeoState state = geoRepository.findOrCreateState("US");
        assertEquals("user5", state.getEligibleUserId());
        DiscountCode code = discountService.generateCode("US");

        // user5 uses the code
        addItemToCart("user5", "p2", 100.0, 1);
        CheckoutResult result = orderService.checkout("user5", "US", code.getCode());

        assertEquals(10.0, result.order().getDiscountAmount(), 0.001);
        assertEquals(90.0, result.order().getTotal(), 0.001);
    }

    @Test
    void checkout_emptyCart_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout("user1", "US", null));
    }

    @Test
    void checkout_invalidDiscountCode_throwsException() {
        addItemToCart("user1", "p1", 50.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout("user1", "US", "BADCODE"));
    }

    @Test
    void checkout_discountCodeUsedTwice_throwsException() {
        for (int i = 1; i <= 5; i++) {
            addItemToCart("user" + i, "p1", 10.0, 1);
            orderService.checkout("user" + i, "US", null);
        }
        DiscountCode code = discountService.generateCode("US");

        addItemToCart("user5", "p2", 100.0, 1);
        orderService.checkout("user5", "US", code.getCode());

        addItemToCart("user5", "p3", 100.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout("user5", "US", code.getCode()));
    }
}
