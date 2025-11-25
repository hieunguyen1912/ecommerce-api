package com.hieu.ecommerce.service;

import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.request.*;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {
    
    // Create
    ProductResponse createProduct(CreateProductRequest request);

    // Update - Granular methods
    ProductResponse updateProductBasicInfo(Long productId, UpdateProductRequest request);

    ProductResponse addVariant(Long productId, CreateProductVariantRequest request);

    ProductResponse removeVariant(Long productId, Long variantId);

    // Query
    Page<ProductSummaryResponse> getAllProducts(Pageable pageable, ProductFilterRequest request);

    ProductResponse getProductById(Long id);
    ProductResponse getActiveProductById(Long id);
    Product getActiveProduct(Long id);
    Integer countProducts();

    // Status management
    void changeStatus(Long id, ProductStatus status);
    void deleteProduct(Long id);
}
