package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.AddToCartRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequest;
import com.hieu.ecommerce.model.dto.response.CartResponse;
import com.hieu.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart", description = "API endpoints for shopping cart management")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

   @PostMapping
   @Operation(summary = "Add item to cart", description = "Add a product variant to the shopping cart")
   @ApiResponses(value = {
           @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
           @ApiResponse(responseCode = "400", description = "Invalid input data")
   })
   @SecurityRequirement(name = "bearerAuth")
   @ResponseMessage("Add item to cart successfully")
   public ResponseEntity<CartResponse> addCart(@RequestBody AddToCartRequest addToCartRequest) {
       CartResponse cartResponse = cartService.addToCart(addToCartRequest);
       return ResponseEntity.ok(cartResponse);
   }

    @PutMapping
    @Operation(summary = "Update cart item", description = "Update quantity of an item in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Update cart item successfully")
    public ResponseEntity<CartResponse> updateCartItem(@RequestBody UpdateCartItemRequest updateCartItemRequest) {
        CartResponse cartResponse = cartService.updateCartItem(updateCartItemRequest);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Remove item from cart successfully")
    public ResponseEntity<Void> removeCartItem(
            @Parameter(description = "Cart item ID", required = true) @PathVariable Long id) {
        cartService.removeCartItem(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Remove all item form cart successfully")
    public ResponseEntity<Void> removeAllCartItems() {
        cartService.clearCart();
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieve current user's shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get user cart successfully")
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cartResponse = cartService.getCart();
        return ResponseEntity.ok(cartResponse);
    }
}
