package com.hieu.ecommerce.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummary {

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "rating_1_count")
    private Integer rating1Count = 0;

    @Column(name = "rating_2_count")
    private Integer rating2Count = 0;

    @Column(name = "rating_3_count")
    private Integer rating3Count = 0;

    @Column(name = "rating_4_count")
    private Integer rating4Count = 0;

    @Column(name = "rating_5_count")
    private Integer rating5Count = 0;

    @Column(name = "verified_review_count")
    private Integer verifiedReviewCount = 0;
}

