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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Product Management", description = "API endpoints for product management (Admin only)")
public class ProductManagementController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;


    @GetMapping
    @Operation(summary = "Get all products (Admin)", description = "Retrieve paginated list of all products with filters. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @Valid @RequestBody ProductFilterRequest request) {
        Page<ProductSummaryResponse> products = productService.getAllProducts(pageable, request);

        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(products));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Create product successfully")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID (Admin)", description = "Retrieve product details by ID. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}/basic-info")
    @Operation(summary = "Update product basic info", description = "Update basic product information. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Update product basic info successfully")
    public ResponseEntity<ApiResponse<ProductResponse>> updateBasicInfo(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        ProductResponse response = productService.updateProductBasicInfo(id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/variants")
    @Operation(summary = "Add product variant", description = "Add a new variant to a product. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Variant added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Add variant successfully")
    public ResponseEntity<ProductResponse> addVariant(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CreateProductVariantRequest request) {
        return ResponseEntity.ok(productService.addVariant(id, request));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    @Operation(summary = "Remove product variant", description = "Remove a variant from a product. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Variant removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or variant not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Remove variant successfully")
    public ResponseEntity<ProductResponse> removeVariant(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id,
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId) {
        return ResponseEntity.ok(productService.removeVariant(id, variantId));
    }

    @PutMapping("/{id}/variants/{variantId}")
    @Operation(summary = "Update product variant", description = "Update a product variant. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Variant updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or variant not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Update variant successfully")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id,
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(productVariantService.updateProductVariant(id, variantId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Delete product successfully")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Change product status", description = "Update product status (ACTIVE, INACTIVE, etc.). Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Change product status successfully")
    public ResponseEntity<Void> changeStatus(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id,
            @Parameter(description = "New product status", required = true) @RequestParam ProductStatus status) {
        productService.changeStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "Get product images", description = "Retrieve all images for a product. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product images retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get product images successfully")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {

        List<ProductImage> images = productImageService.getImagesByProductId(id);
        List<ProductImageResponse> responses = images.stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/thumbnail")
    @Operation(summary = "Get product thumbnail", description = "Retrieve thumbnail image for a product. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thumbnail retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get thumbnail successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> getProductThumbnail(
            @Parameter(description = "Product ID", required = true) @PathVariable Long id) {

        ProductImage thumbnail = productImageService.getThumbnailByProductId(id);
        if (thumbnail == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(toImageResponse(thumbnail)));
    }

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
