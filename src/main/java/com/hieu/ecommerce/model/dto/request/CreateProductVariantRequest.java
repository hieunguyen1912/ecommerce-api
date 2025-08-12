package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductVariantRequest {
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers and hyphens")
    private String sku;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotEmpty(message = "Attribute values are required")
    private List<CreateAttributeValueRequest> attributeValues;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;
}
