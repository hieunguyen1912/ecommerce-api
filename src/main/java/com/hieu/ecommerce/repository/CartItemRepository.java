package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Cart;
import com.hieu.ecommerce.model.entity.CartItem;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findByCartAndProductAndProductVariant(Cart cart, Product product, ProductVariant productVariant);
    CartItem findByCartAndProductAndProductVariantIsNull(Cart cart, Product product);
    List<CartItem> findByCart(Cart cart);
}
