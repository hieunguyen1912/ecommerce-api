package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.request.UpdateReviewRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;
import com.hieu.ecommerce.service.ReviewService;
import com.hieu.ecommerce.util.PaginationHelper;
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
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest createReviewRequest) {
        ReviewResponse reviewResponse = reviewService.createReview(createReviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponse);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest updateReviewRequest) {

        ReviewResponse reviewResponse = reviewService.updateReview(reviewId, updateReviewRequest);

        return ResponseEntity.status(HttpStatus.OK).body(reviewResponse);
    }

    @DeleteMapping("/{reviewId}")
    @ResponseMessage("delete review successfully")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
            Pageable pageable,
            @PathVariable Long productId) {
        Page<ReviewResponse> responses = reviewService.getAllReviews(pageable, productId);
        PageResponse<ReviewResponse> pageResponse = PaginationHelper.toPaginatedResponse(responses);
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }


}


