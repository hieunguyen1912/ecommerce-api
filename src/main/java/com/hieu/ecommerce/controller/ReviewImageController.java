package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.service.ReviewService;
import com.hieu.ecommerce.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewImageController {

    private final StorageService storageService;
    private final ReviewService reviewService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = storageService.upload(file, "uploads/reviews");
        return ResponseEntity.ok(fileUrl);
    }

    @DeleteMapping("/{reviewId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long reviewId,
            @PathVariable Long imageId) {
        reviewService.deleteImage(reviewId, imageId);
        return ResponseEntity.noContent().build();
    }
}
