package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest createReviewRequest);
}
