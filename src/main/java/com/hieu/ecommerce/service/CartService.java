package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.AddToCartRequestDTO;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequestDTO;
import com.hieu.ecommerce.model.dto.response.CartResponseDTO;

public interface CartService {

    CartResponseDTO getCart(Long userId);

    CartResponseDTO addToCart(Long userId, AddToCartRequestDTO request);

    CartResponseDTO updateCartItem(Long userId, UpdateCartItemRequestDTO request);

    void removeCartItem(Long userId, Long cartItemId);

    //OrderResponseDTO checkout(Long userId, CheckoutRequest request);

    void clearCart(Long userId);
}
