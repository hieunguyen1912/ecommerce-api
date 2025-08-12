package com.hieu.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "product_variant_images")
@Getter
@Setter
public class ProductVariantImage extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    @JsonIgnore
    private ProductVariant productVariant;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private boolean isDefault;
}
