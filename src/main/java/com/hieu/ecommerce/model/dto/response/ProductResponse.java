package com.hieu.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean hasVariants;
    private Instant createdAt;
    private List<String> imageUrls;
    private List<CategoryResponse> categories;
    private List<ProductVariantResponse> variants;
}
