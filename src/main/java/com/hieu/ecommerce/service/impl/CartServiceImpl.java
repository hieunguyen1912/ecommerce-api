package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.constant.VariantStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.CartMapper;
import com.hieu.ecommerce.model.dto.request.AddToCartRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequest;
import com.hieu.ecommerce.model.dto.response.CartResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.*;
import com.hieu.ecommerce.service.*;
import com.hieu.ecommerce.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public CartResponse getCart() {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Product product = productRepository.findByIdAndStatus(request.getProductId(), ProductStatus.ACTIVE)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        ProductVariant productVariant = productVariantRepository.findByIdAndStatus(request.getProductVariantId(), VariantStatus.ACTIVE)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!productVariant.getProduct().getId().equals(product.getId())) {
            throw new AppException(ErrorCode.INVALID_OPERATION, "Variant does not belong to this product");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user).build();
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductAndProductVariant(cart, product, productVariant);

        int totalQuantity = existingItem.map(item -> item.getQuantity() + request.getQuantity())
                .orElseGet(request::getQuantity);

        if (totalQuantity > productVariant.getStock()) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(totalQuantity);
            existingItem.get().setPrice(productVariant.getPrice());
            cartItemRepository.save(existingItem.get());
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .productVariant(productVariant)
                    .price(productVariant.getPrice())
                    .quantity(request.getQuantity()).build();
            cartItemRepository.save(cartItem);
        }

        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(UpdateCartItemRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        CartItem cartItem = cartItemRepository.findByIdAndCart_User(request.getCartItemId(), user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                    "Cart item not found or does not belong to user"));

        Product product = cartItem.getProduct();
        ProductVariant productVariant = cartItem.getProductVariant();

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_OPERATION, "Product is not available");
        }

        if (productVariant.getStatus() != VariantStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_OPERATION, "Product variant is not available");
        }

        if (request.getQuantity() > productVariant.getStock()) {
            throw new AppException(ErrorCode.INVALID_OPERATION, 
                "Insufficient stock. Available: " + productVariant.getStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Cart not found"));

        return cartMapper.toCartResponse(cart);
    }
    
    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        CartItem cartItem = cartItemRepository.findByIdAndCart_User(cartItemId, user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Cart item not found or does not belong to user"));

        cartItemRepository.delete(cartItem);
    }
    
    @Override
    @Transactional
    public void clearCart() {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        cartItemRepository.deleteAll(cartItems);
    }

}
