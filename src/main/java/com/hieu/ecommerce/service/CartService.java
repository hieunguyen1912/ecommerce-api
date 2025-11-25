package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.AddToCartRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequest;
import com.hieu.ecommerce.model.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart();

    CartResponse addToCart(AddToCartRequest request);

    CartResponse updateCartItem(UpdateCartItemRequest request);

    void removeCartItem(Long cartItemId);

    void clearCart();
}
