package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateProductVariantRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.entity.Product;

public interface ProductVariantService {
    void createProductVariant(CreateProductVariantRequest createProductVariantRequest, Product product);

    void updateProductVariants(Product product, UpdateProductRequest updateProductRequest);
}
