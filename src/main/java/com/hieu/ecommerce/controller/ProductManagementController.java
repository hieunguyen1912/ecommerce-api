package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.request.*;
import com.hieu.ecommerce.model.dto.response.ApiResponse;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.dto.response.ProductVariantResponse;
import com.hieu.ecommerce.model.dto.response.ProductImageResponse;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.service.ProductImageService;
import com.hieu.ecommerce.service.ProductService;
import com.hieu.ecommerce.service.ProductVariantService;

import java.util.List;
import java.util.stream.Collectors;
import com.hieu.ecommerce.util.PaginationHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductManagementController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;


    @GetMapping
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @Valid @RequestBody ProductFilterRequest request) {
        Page<ProductSummaryResponse> products = productService.getAllProducts(pageable, request);

        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(products));
    }

    @PostMapping
    @ResponseMessage("Create product successfully")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}/basic-info")
    @ResponseMessage("Update product basic info successfully")
    public ResponseEntity<ApiResponse<ProductResponse>> updateBasicInfo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        ProductResponse response = productService.updateProductBasicInfo(id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/variants")
    @ResponseMessage("Add variant successfully")
    public ResponseEntity<ProductResponse> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductVariantRequest request) {
        return ResponseEntity.ok(productService.addVariant(id, request));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    @ResponseMessage("Remove variant successfully")
    public ResponseEntity<ProductResponse> removeVariant(
            @PathVariable Long id,
            @PathVariable Long variantId) {
        return ResponseEntity.ok(productService.removeVariant(id, variantId));
    }

    @PutMapping("/{id}/variants/{variantId}")
    @ResponseMessage("Update variant successfully")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(
            @PathVariable Long id,
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(productVariantService.updateProductVariant(id, variantId, request));
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete product successfully")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    @ResponseMessage("Change product status successfully")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                              @RequestParam ProductStatus status) {
        productService.changeStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/images")
    @ResponseMessage("Get product images successfully")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(
            @PathVariable Long id) {

        List<ProductImage> images = productImageService.getImagesByProductId(id);
        List<ProductImageResponse> responses = images.stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/thumbnail")
    @ResponseMessage("Get thumbnail successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> getProductThumbnail(
            @PathVariable Long id) {

        ProductImage thumbnail = productImageService.getThumbnailByProductId(id);
        if (thumbnail == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(toImageResponse(thumbnail)));
    }

    /**
     * Helper method: Convert ProductImage entity to ProductImageResponse
     */
    private ProductImageResponse toImageResponse(ProductImage productImage) {
        if (productImage == null) {
            return null;
        }
        
        ProductImageResponse response = new ProductImageResponse();
        response.setId(productImage.getId());
        response.setProductId(productImage.getProduct().getId());
        response.setProductVariantId(productImage.getProductVariant().getId());
        response.setImageUrl(productImage.getImageUrl());
        response.setThumbnail(productImage.isThumbnail());
        response.setCreatedAt(productImage.getCreatedAt());
        response.setUpdatedAt(productImage.getUpdatedAt());
        return response;
    }
}
