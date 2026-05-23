package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.GeoRepository;
import com.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final GeoRepository geoRepository;
    private final CartService cartService;
    private final DiscountService discountService;

    public OrderService(OrderRepository orderRepository,
                        GeoRepository geoRepository,
                        CartService cartService,
                        DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.geoRepository = geoRepository;
        this.cartService = cartService;
        this.discountService = discountService;
    }

    public CheckoutResult checkout(String userId, String geoId, String discountCode) {
        log.info("Checkout initiated [userId={}, geoId={}, hasDiscountCode={}]",
                userId, geoId, discountCode != null && !discountCode.isBlank());

        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            log.warn("Checkout rejected — cart is empty [userId={}]", userId);
            throw new IllegalStateException("Cart is empty");
        }

        GeoConfig config = geoRepository.findConfigByGeoId(geoId)
                .orElseThrow(() -> {
                    log.warn("Checkout rejected — geo not configured [geoId={}]", geoId);
                    return new IllegalArgumentException("Geo not configured: " + geoId);
                });

        double subtotal = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double discountAmount = 0;
        DiscountCode appliedCode = null;

        if (discountCode != null && !discountCode.isBlank()) {
            appliedCode = discountService.validateCode(discountCode, userId, geoId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired discount code"));
            discountAmount = discountService.applyDiscount(subtotal, appliedCode);
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getName(), i.getPrice(), i.getQuantity()))
                .collect(Collectors.toList());

        Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(userId)
                .geoId(geoId)
                .items(orderItems)
                .subtotal(subtotal)
                .discountCode(discountCode)
                .discountAmount(discountAmount)
                .total(subtotal - discountAmount)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        log.info("Order created [orderId={}, userId={}, geoId={}, subtotal={}, discountAmount={}, total={}]",
                order.getOrderId(), userId, geoId, subtotal, discountAmount, order.getTotal());

        if (appliedCode != null) {
            discountService.markAsUsed(appliedCode);
        }

        cartService.clearCart(userId);

        // Update geo order count and check nth order condition
        GeoState state = geoRepository.findOrCreateState(geoId);
        state.setOrderCount(state.getOrderCount() + 1);

        boolean nthOrderReached = state.getOrderCount() % config.getNthOrder() == 0;
        if (nthOrderReached) {
            log.info("nth order reached [geo={}, orderCount={}, eligibleUserId={}]",
                    geoId, state.getOrderCount(), userId);
            state.setPendingCodeGeneration(true);
            state.setEligibleUserId(userId);
        }

        geoRepository.saveState(state);

        return new CheckoutResult(order, nthOrderReached);
    }
}
