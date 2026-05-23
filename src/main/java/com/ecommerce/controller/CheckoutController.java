package com.ecommerce.controller;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.CheckoutResponse;
import com.ecommerce.model.CheckoutResult;
import com.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest request) {
        CheckoutResult result = orderService.checkout(
                request.userId(),
                request.geoId(),
                request.discountCode()
        );

        CheckoutResponse response = new CheckoutResponse(
                result.order().getOrderId(),
                result.order().getSubtotal(),
                result.order().getDiscountAmount(),
                result.order().getTotal(),
                result.nthOrderReached()
        );

        return ResponseEntity.ok(response);
    }
}
