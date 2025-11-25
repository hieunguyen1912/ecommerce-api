package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.AddToCartRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequest;
import com.hieu.ecommerce.model.dto.response.CartResponse;
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
   @ResponseMessage("Add item to cart successfully")
   public ResponseEntity<CartResponse> addCart(@RequestBody AddToCartRequest addToCartRequest) {
       CartResponse cartResponse = cartService.addToCart(addToCartRequest);
       return ResponseEntity.ok(cartResponse);
   }

    @PutMapping
    @ResponseMessage("Update cart item successfully")
    public ResponseEntity<CartResponse> updateCartItem(@RequestBody UpdateCartItemRequest updateCartItemRequest) {
        CartResponse cartResponse = cartService.updateCartItem(updateCartItemRequest);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Remove item from cart successfully")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long id) {
        cartService.removeCartItem(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @ResponseMessage("Remove all item form cart successfully")
    public ResponseEntity<Void> removeAllCartItems() {
        cartService.clearCart();
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @ResponseMessage("Get user cart successfully")
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cartResponse = cartService.getCart();
        return ResponseEntity.ok(cartResponse);
    }
}
