package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.constant.VariantStatus;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsByProductAndSku(Product product, String sku);
    Optional<ProductVariant> findByIdAndStatus(Long id, VariantStatus status);
}
