package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductAndProductVariant(Cart cart, Product product, ProductVariant productVariant);
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByIdAndCart_User(Long cartItemId, User user);
}
