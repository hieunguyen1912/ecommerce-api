package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.common.SecurityUtil;
import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.AddToCartRequestDTO;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequestDTO;
import com.hieu.ecommerce.model.dto.response.CartResponseDTO;
import com.hieu.ecommerce.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponseDTO> addCart(@RequestBody AddToCartRequestDTO addToCartRequestDTO) {
        Long id = SecurityUtil.getCurrentUserId();
        CartResponseDTO cartResponseDTO = cartService.addToCart(id, addToCartRequestDTO);
        return ResponseEntity.ok(cartResponseDTO);
    }

    @PutMapping
    public ResponseEntity<CartResponseDTO> updateCartItem(@RequestBody UpdateCartItemRequestDTO updateCartItemRequestDTO) {
        Long id = SecurityUtil.getCurrentUserId();
        CartResponseDTO cartResponseDTO = cartService.updateCartItem(id, updateCartItemRequestDTO);
        return ResponseEntity.ok(cartResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Remove item from cart successfully")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.removeCartItem(userId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @ResponseMessage("Remove all item form cart successfully")
    public ResponseEntity<Void> removeAllCartItems() {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
