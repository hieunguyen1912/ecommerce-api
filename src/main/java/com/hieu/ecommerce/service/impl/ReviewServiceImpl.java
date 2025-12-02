package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.OrderStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.ReviewMapper;
import com.hieu.ecommerce.model.dto.request.CreateReviewRequest;
import com.hieu.ecommerce.model.dto.request.UpdateReviewRequest;
import com.hieu.ecommerce.model.dto.response.ReviewResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.*;
import com.hieu.ecommerce.service.ReviewService;
import com.hieu.ecommerce.service.StorageService;
import com.hieu.ecommerce.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StorageService storageService;
    private final ReviewImageRepository reviewImageRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest createReviewRequest) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        OrderItem orderItem = orderItemRepository.findById(createReviewRequest.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        Order order = orderItem.getOrder();

        if (!order.getUser().getId().equals(currentUserId)) {
                throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Order item does not belong to current user");
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

        Review review = Review.builder()
                .orderItem(orderItem)
                .rating(createReviewRequest.getRating())
                .title(createReviewRequest.getTitle())
                .comment(createReviewRequest.getComment())
                .build();
        reviewRepository.save(review);


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

        Product product = orderItem.getProduct();
        updateProductRating(product, 0, createReviewRequest.getRating());

        return reviewMapper.toReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest updateReviewRequest) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User reviewOwner = review.getOrderItem().getOrder().getUser();

        if (!reviewOwner.getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You are not allowed to edit this review");
        }

        Integer oldRating = review.getRating();
        Integer newRating = updateReviewRequest.getRating();

        review.setTitle(updateReviewRequest.getTitle());
        review.setComment(updateReviewRequest.getComment());
        review.setRating(newRating);

        reviewRepository.save(review);

        if (!oldRating.equals(newRating)) {
            updateProductRating(review.getOrderItem().getProduct(), oldRating, newRating);
        }

        if (updateReviewRequest.getImageUrls() != null && !updateReviewRequest.getImageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            for (int i = 0; i < updateReviewRequest.getImageUrls().size(); i++) {
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(review)
                        .imageUrl(updateReviewRequest.getImageUrls().get(i))
                        .displayOrder(i)
                        .build();
                images.add(reviewImage);
            }
            review.setImages(images);
        } else {
            review.setImages(new ArrayList<>());
        }

        return reviewMapper.toReviewResponse(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User reviewOwner = review.getOrderItem().getOrder().getUser();

        if (!reviewOwner.getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You are not allowed to edit this review");
        }

        List<ReviewImage> images = review.getImages();
        for (ReviewImage image : images) {
            storageService.delete(image.getImageUrl());
        }

        reviewRepository.delete(review);
    }

    @Override
    public Page<ReviewResponse> getAllReviews(Pageable pageable, Long productId) {
        Page<Review> review = reviewRepository.findByProduct_Id(productId, pageable);

        return review.map(reviewMapper::toReviewResponse);
    }

    @Override
    public void deleteImage(Long reviewId, Long imageId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        ReviewImage reviewImage = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User reviewOwner = review.getOrderItem().getOrder().getUser();

        if (!reviewOwner.getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You are not allowed to edit this review");
        }
        storageService.delete(reviewImage.getImageUrl());
        reviewImageRepository.deleteById(imageId);
    }

    private void updateProductRating(Product product, int oldRating, int newRating) {

        switch (oldRating) {
            case 1 -> product.setRating1Count(product.getRating1Count() - 1);
            case 2 -> product.setRating2Count(product.getRating2Count() - 1);
            case 3 -> product.setRating3Count(product.getRating3Count() - 1);
            case 4 -> product.setRating4Count(product.getRating4Count() - 1);
            case 5 -> product.setRating5Count(product.getRating5Count() - 1);
        }

        switch (newRating) {
            case 1 -> product.setRating1Count(product.getRating1Count() + 1);
            case 2 -> product.setRating2Count(product.getRating2Count() + 1);
            case 3 -> product.setRating3Count(product.getRating3Count() + 1);
            case 4 -> product.setRating4Count(product.getRating4Count() + 1);
            case 5 -> product.setRating5Count(product.getRating5Count() + 1);
        }

        int totalReviews = product.getRating1Count()
                + product.getRating2Count()
                + product.getRating3Count()
                + product.getRating4Count()
                + product.getRating5Count();

        int totalScore =
                product.getRating1Count() +
                        product.getRating2Count() * 2 +
                        product.getRating3Count() * 3 +
                        product.getRating4Count() * 4 +
                        product.getRating5Count() * 5;

        BigDecimal avgRating = totalReviews > 0
                ? new BigDecimal(String.format("%.2f", (double) totalScore / totalReviews))
                : BigDecimal.ZERO;

        product.setTotalReviews(totalReviews);
        product.setAverageRating(avgRating);

        productRepository.save(product);
    }
}
