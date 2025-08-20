package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductVariantRequest;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.ProductVariantImage;

import java.util.List;

public interface ImageService {
    List<ProductImage> createProductImages(List<String> imageUrls, Product product);

    void updateProductImages(Product product, UpdateProductRequest updateProductRequest);

    void updateVariantImages(ProductVariant productVariant, UpdateProductVariantRequest variantReq);

    ProductVariantImage buildProductVariantImage(String imageUrl, ProductVariant productVariant, boolean isDefault);
}
