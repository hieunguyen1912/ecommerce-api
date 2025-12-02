package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.request.UpdateReviewRequest;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest createReviewRequest);

    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest updateReviewRequest);

    void deleteReview(Long reviewId);

    Page<ReviewResponse> getAllReviews(Pageable pageable, Long productId);

    void deleteImage(Long reviewId, Long imageId);
}
