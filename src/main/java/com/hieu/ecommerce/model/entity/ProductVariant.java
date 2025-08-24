package com.hieu.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ecommerce.common.annotation.EnumPattern;
import com.hieu.ecommerce.common.constant.VariantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_variants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "sku"}))
public class ProductVariant extends Auditable{

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(length = 50, nullable = false)
    private String sku;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity must be zero or greater")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @EnumPattern(name = "Product status", regexp = "^(ACTIVE|INACTIVE|DELETED|OUT_OF_STOCK)$", message = "Product status must be one of: ACTIVE, INACTIVE, DELETED, OUT_OF_STOCK")
    //@Column(nullable = false)
    private VariantStatus status = VariantStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "VariantAttributeValues",
        joinColumns = @JoinColumn(name = "product_variant_id"),
        inverseJoinColumns = @JoinColumn(name = "attribute_value_id")
    )
    private List<AttributeValue> attributeValues = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantImage> images = new ArrayList<>();
}
