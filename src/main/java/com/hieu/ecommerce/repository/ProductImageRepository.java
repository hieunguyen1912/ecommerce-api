package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Find the default image for a product.
     *
     * @param productId the ID of the product
     * @return the default ProductImage or null if not found
     */
    ProductImage findDefaultImageByProductId(Long productId);

    /**
     * Find all images for a product.
     *
     * @param productId the ID of the product
     * @return a list of ProductImage objects
     */
    List<ProductImage> findAllByProductId(Long productId);
}
