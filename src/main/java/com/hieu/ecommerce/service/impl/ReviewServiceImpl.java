package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.OrderStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.ReviewMapper;
import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.OrderItemRepository;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.repository.ReviewRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.ReviewService;
import com.hieu.ecommerce.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest createReviewRequest) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Product product = productRepository.findById(createReviewRequest.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        OrderItem orderItem = orderItemRepository.findById(createReviewRequest.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        Order order = orderItem.getOrder();
        if (!order.getUser().getId().equals(currentUserId)) {
                throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Order item does not belong to current user");
        }

        if (!orderItem.getProduct().getId().equals(createReviewRequest.getProductId())) {
                throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Order item does not belong to the specified product");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
                throw new AppException(ErrorCode.INVALID_OPERATION, 
                    "Cannot review product from order with status: " + order.getOrderStatus() + 
                    ". Order must be DELIVERED");
        }

        if (orderItem.getReview() != null) {
                throw new AppException(ErrorCode.DUPLICATE_RESOURCE, 
                    "This order item has already been reviewed");
        }

        Review review = reviewMapper.toReview(createReviewRequest);
        review.setProduct(product);
        review.setUser(user);
        review.setOrderItem(orderItem);

        if (createReviewRequest.getImageUrls() != null && !createReviewRequest.getImageUrls().isEmpty()) {
                List<ReviewImage> images = new ArrayList<>();
                for (int i = 0; i < createReviewRequest.getImageUrls().size(); i++) {
                    ReviewImage reviewImage = ReviewImage.builder()
                            .review(review)
                            .imageUrl(createReviewRequest.getImageUrls().get(i))
                            .displayOrder(i)
                            .build();
                    images.add(reviewImage);
                }
                review.setImages(images);
        }

        reviewRepository.save(review);
        return reviewMapper.toReviewResponse(review);
    }
}
