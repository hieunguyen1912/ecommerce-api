package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateProductVariantRequest;
import com.hieu.ecommerce.model.dto.request.UpdateVariantRequest;
import com.hieu.ecommerce.model.dto.response.ProductVariantResponse;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;

import java.util.List;

public interface ProductVariantService {
    void createProductVariants(List<CreateProductVariantRequest> variantRequests, Product product);

    ProductVariant createSingleVariant(CreateProductVariantRequest createProductVariantRequest, Product product);

    void addVariant(Product product, CreateProductVariantRequest request);

    void removeVariant(Product product, Long variantId);

    ProductVariantResponse updateProductVariant(Long productId, Long variantId, UpdateVariantRequest request);
}
