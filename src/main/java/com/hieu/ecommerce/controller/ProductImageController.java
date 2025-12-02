package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.MoveImageRequest;
import com.hieu.ecommerce.model.dto.response.ApiResponse;
import com.hieu.ecommerce.model.dto.response.ProductImageResponse;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/variants/{variantId}/images")
@RequiredArgsConstructor
@Tag(name = "Product Images", description = "API endpoints for product image management (Admin only)")
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload product image", description = "Upload an image for a product variant. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or variant not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Upload image successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId,
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId,
            @Parameter(description = "Image file to upload", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Set as thumbnail", example = "false") @RequestParam(value = "isThumbnail", defaultValue = "false") boolean isThumbnail) {

        ProductImage productImage = productImageService.uploadImage(productId, variantId, file, isThumbnail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(productImage)));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple product images", description = "Upload multiple images for a product variant. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Images uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or variant not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Upload images successfully")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadMultipleImages(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId,
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId,
            @Parameter(description = "Image files to upload", required = true) @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Set first image as thumbnail", example = "false") @RequestParam(value = "setFirstAsThumbnail", defaultValue = "false") boolean setFirstAsThumbnail) {

        List<ProductImage> images = productImageService.uploadMultipleImages(
                productId, variantId, files, setFirstAsThumbnail);
        
        List<ProductImageResponse> responses = images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responses));
    }

    @GetMapping
    @Operation(summary = "Get images by variant", description = "Retrieve all images for a product variant. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Variant not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get images successfully")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImagesByVariant(
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId) {

        List<ProductImage> images = productImageService.getImagesByVariantId(variantId);
        
        List<ProductImageResponse> responses = images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }


    @GetMapping("/{imageId}")
    @Operation(summary = "Get image by ID", description = "Retrieve product image details by ID. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get image successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> getImageById(
            @Parameter(description = "Image ID", required = true) @PathVariable Long imageId) {

        ProductImage productImage = productImageService.getImageById(imageId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(productImage)));
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete product image", description = "Delete a product image. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Delete image successfully")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Image ID", required = true) @PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{imageId}/thumbnail")
    @Operation(summary = "Set thumbnail", description = "Set an image as product thumbnail. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thumbnail set successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Set thumbnail successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> setThumbnail(
            @Parameter(description = "Image ID", required = true) @PathVariable Long imageId) {

        ProductImage productImage = productImageService.setThumbnail(imageId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(productImage)));
    }

    @PutMapping("/{imageId}/move")
    @Operation(summary = "Move image to another variant", description = "Move an image from one variant to another. Requires ADMIN role")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image moved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image or target variant not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Move image successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> moveImage(
            @Parameter(description = "Image ID", required = true) @PathVariable Long imageId,
            @Valid @RequestBody MoveImageRequest request) {

        ProductImage productImage = productImageService.moveImageToVariant(
                imageId, request.getNewProductId(), request.getNewVariantId());
        return ResponseEntity.ok(ApiResponse.success(toResponse(productImage)));
    }


    private ProductImageResponse toResponse(ProductImage productImage) {
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

