package com.hieu.ecommerce.model.dto.request;

import com.hieu.ecommerce.constant.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductFilterRequest {

    private String categoryName;

    private List<ProductStatus> statuses;
    private List<Long> categoryIds;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Integer minRating;
}
