package com.hieu.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariantResponse {
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<AttributeValueResponse> attributeValues;
    private List<String> imageUrls;
}