package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.ProductVariantImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, Long> {
} 