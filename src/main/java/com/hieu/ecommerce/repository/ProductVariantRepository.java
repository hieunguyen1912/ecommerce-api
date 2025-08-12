package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsByProductAndSku(Product product, String sku);
}
