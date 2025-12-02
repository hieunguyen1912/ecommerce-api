package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.service.ReviewService;
import com.hieu.ecommerce.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Images", description = "API endpoints for review image management")
public class ReviewImageController {

    private final StorageService storageService;
    private final ReviewService reviewService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload review image", description = "Upload an image for a review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> uploadImage(
            @Parameter(description = "Image file to upload", required = true) @RequestParam("file") MultipartFile file) {
        String fileUrl = storageService.upload(file, "uploads/reviews");
        return ResponseEntity.ok(fileUrl);
    }

    @DeleteMapping("/{reviewId}/images/{imageId}")
    @Operation(summary = "Delete review image", description = "Delete an image from a review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review or image not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this image")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "Image ID", required = true) @PathVariable Long imageId) {
        reviewService.deleteImage(reviewId, imageId);
        return ResponseEntity.noContent().build();
    }
}
