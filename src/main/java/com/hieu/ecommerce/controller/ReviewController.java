package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.request.UpdateReviewRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;
import com.hieu.ecommerce.service.ReviewService;
import com.hieu.ecommerce.util.PaginationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API endpoints for product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create review", description = "Create a new product review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest createReviewRequest) {
        ReviewResponse reviewResponse = reviewService.createReview(createReviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponse);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update review", description = "Update an existing review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this review")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest updateReviewRequest) {

        ReviewResponse reviewResponse = reviewService.updateReview(reviewId, updateReviewRequest);

        return ResponseEntity.status(HttpStatus.OK).body(reviewResponse);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this review")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("delete review successfully")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get reviews by product", description = "Retrieve paginated reviews for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
            Pageable pageable,
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        Page<ReviewResponse> responses = reviewService.getAllReviews(pageable, productId);
        PageResponse<ReviewResponse> pageResponse = PaginationHelper.toPaginatedResponse(responses);
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }


}


