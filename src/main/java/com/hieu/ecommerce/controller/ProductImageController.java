package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.MoveImageRequest;
import com.hieu.ecommerce.model.dto.response.ApiResponse;
import com.hieu.ecommerce.model.dto.response.ProductImageResponse;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.service.ProductImageService;
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
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseMessage("Upload image successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isThumbnail", defaultValue = "false") boolean isThumbnail) {

        ProductImage productImage = productImageService.uploadImage(productId, variantId, file, isThumbnail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(productImage)));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseMessage("Upload images successfully")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadMultipleImages(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "setFirstAsThumbnail", defaultValue = "false") boolean setFirstAsThumbnail) {

        List<ProductImage> images = productImageService.uploadMultipleImages(
                productId, variantId, files, setFirstAsThumbnail);
        
        List<ProductImageResponse> responses = images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responses));
    }

    @GetMapping
    @ResponseMessage("Get images successfully")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImagesByVariant(
            @PathVariable Long variantId) {

        List<ProductImage> images = productImageService.getImagesByVariantId(variantId);
        
        List<ProductImageResponse> responses = images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }


    @GetMapping("/{imageId}")
    @ResponseMessage("Get image successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> getImageById(
            @PathVariable Long imageId) {

        ProductImage productImage = productImageService.getImageById(imageId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(productImage)));
    }

    @DeleteMapping("/{imageId}")
    @ResponseMessage("Delete image successfully")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{imageId}/thumbnail")
    @ResponseMessage("Set thumbnail successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> setThumbnail(
            @PathVariable Long imageId) {

        ProductImage productImage = productImageService.setThumbnail(imageId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(productImage)));
    }

    @PutMapping("/{imageId}/move")
    @ResponseMessage("Move image successfully")
    public ResponseEntity<ApiResponse<ProductImageResponse>> moveImage(
            @PathVariable Long imageId,
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

