package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;
import com.hieu.ecommerce.model.entity.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    Review toReview(CreateReviewRequest request);

    ReviewResponse toReviewResponse(Review review);
}
