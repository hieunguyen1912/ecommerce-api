package com.hieu.ecommerce.model.entity;

import com.hieu.ecommerce.annotation.EnumPattern;
import com.hieu.ecommerce.constant.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product extends BaseEntity{

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Enumerated(EnumType.STRING)
    @EnumPattern(name = "Product status", regexp = "^(ACTIVE|INACTIVE|DELETED|OUT_OF_STOCK)$", message = "Product status must be one of: ACTIVE, INACTIVE, DELETED")
    private ProductStatus status = ProductStatus.ACTIVE;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> productVariant = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private int totalReviews = 0;

    @Column(name = "rating_1_count")
    @Builder.Default
    private int rating1Count = 0;

    @Column(name = "rating_2_count")
    @Builder.Default
    private int rating2Count = 0;

    @Column(name = "rating_3_count")
    @Builder.Default
    private int rating3Count = 0;

    @Column(name = "rating_4_count")
    @Builder.Default
    private int rating4Count = 0;

    @Column(name = "rating_5_count")
    @Builder.Default
    private int rating5Count = 0;
}
