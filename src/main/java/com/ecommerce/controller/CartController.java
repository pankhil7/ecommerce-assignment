package com.ecommerce.controller;

import com.ecommerce.dto.request.AddToCartRequest;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestBody AddToCartRequest request) {
        CartItem item = new CartItem(
                request.productId(),
                request.name(),
                request.price(),
                request.quantity()
        );
        Cart cart = cartService.addItem(request.userId(), request.geoId(), item);
        return ResponseEntity.ok(cart);
    }
}
