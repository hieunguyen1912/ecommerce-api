package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;

import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest createProductRequest);
    List<ProductSummaryResponse> getAllProducts(Pageable pageable);
    ProductResponse getProductById(Long id);
    Integer countProducts();
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}
