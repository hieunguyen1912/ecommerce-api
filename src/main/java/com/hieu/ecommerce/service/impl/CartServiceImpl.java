package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.common.constant.VariantStatus;
import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.CartMapper;
import com.hieu.ecommerce.model.dto.request.AddToCartRequestDTO;
import com.hieu.ecommerce.model.dto.request.UpdateCartItemRequestDTO;
import com.hieu.ecommerce.model.dto.response.CartResponseDTO;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.CartItemRepository;
import com.hieu.ecommerce.repository.CartRepository;
import com.hieu.ecommerce.service.CartService;
import com.hieu.ecommerce.service.ProductService;
import com.hieu.ecommerce.service.ProductVariantService;
import com.hieu.ecommerce.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;
    private final ProductVariantService productVariantService;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService, UserService userService, ProductVariantService productVariantService, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.userService = userService;
        this.productVariantService = productVariantService;
        this.cartMapper = cartMapper;
    }

    @Override
    public CartResponseDTO getCart(Long userId) {
        User user = userService.getUser(userId);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartMapper.convertCartToCartResponseDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO addToCart(Long userId, AddToCartRequestDTO request) {
        Product product = productService.getActiveProduct(request.getProductId());
        User user = userService.getUser(userId);
        ProductVariant variant = productVariantService.getProductVariant(request.getProductVariantId());

        productService.validateStock(product, variant, request.getQuantity());

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
        CartItem existingItem = findExistingCartItem(cart, product, variant);

        if (existingItem != null) {
            updateExistingCartItem(existingItem, request.getQuantity(), product, variant);
        } else {
            CartItem newCartItem = createNewCartItem(cart, product, variant, request.getQuantity());
            cart.getItems().add(newCartItem);
        }

        return cartMapper.convertCartToCartResponseDTO(cart);
    }

    private void updateExistingCartItem(CartItem existingItem, int quantity, Product product, ProductVariant productVariant) {
        int newQuantity = existingItem.getQuantity() + quantity;
        productService.validateStock(product, productVariant, newQuantity);
        existingItem.setQuantity(newQuantity);
        existingItem.setPrice(productVariant != null ? productVariant.getPrice() : product.getPrice());
        cartItemRepository.save(existingItem);
    }

    private CartItem createNewCartItem(Cart cart, Product product, ProductVariant productVariant, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(productVariant != null ? productVariant.getPrice() : product.getPrice());
        cartItem.setCart(cart);
        cartItem.setProductVariant(productVariant);
        return cartItemRepository.save(cartItem);
    }

    private CartItem findExistingCartItem(Cart cart, Product product, ProductVariant variant) {
        if (variant != null) {
            return cartItemRepository.findByCartAndProductAndProductVariant(cart, product, variant);
        }
        return cartItemRepository.findByCartAndProductAndProductVariantIsNull(cart, product);
    }

    @Override
    public CartResponseDTO updateCartItem(Long userId, UpdateCartItemRequestDTO request) {
        CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem Not Found"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("cart user id not match");
        }

        if (cartItem.getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("cart product status not active");
        }

        productService.validateStock(cartItem.getProduct(), cartItem.getProductVariant(), request.getQuantity());

        if (!cartItem.getProductVariant().getStatus().equals(VariantStatus.ACTIVE)) {
            throw new ResourceNotFoundException("cart product status not active");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return cartMapper.convertCartToCartResponseDTO(cartItem.getCart());
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem Not Found"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("cart user id not match");
        }

        if (cartItem.getProduct().getStatus() == ProductStatus.DELETED) {
            throw new ResourceNotFoundException("product have already deleted");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        User user = userService.getUser(userId);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }
}
