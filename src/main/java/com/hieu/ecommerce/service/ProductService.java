package com.hieu.ecommerce.service;

import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;

import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.Category;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.Shop;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product createBaseProduct(CreateProductRequest createProductRequest, Shop shop, List<Category> categories);

    List<ProductSummaryResponse> getAllProductsByShopId(Pageable pageable);
    List<ProductSummaryResponse> getAllProductsForUser(Pageable pageable);
    List<ProductSummaryResponse> getAllProductsForAdmin(Pageable pageable);

    ProductResponse getProductById(Long id);
    ProductResponse getProductByIdAndShopId(Long id);
    ProductResponse getActiveProductById(Long id);

    Product getActiveProduct(Long id);

    void validateStock(Product product, ProductVariant productVariant, Integer requiredQuantity);

    Integer countProducts();

    void updateProduct(Product product, UpdateProductRequest updateProductRequest);

    void changeStatus(Long id, ProductStatus status);
    void deleteProductForShop(Long id);
    void deleteProductForAdmin(Long id);
}
