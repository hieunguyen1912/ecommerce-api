package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    ProductImage findDefaultImageByProductId(Long productId);

    List<ProductImage> findAllByProductId(Long productId);

    List<ProductImage> findAllByProductVariantId(Long productVariantId);
}
