package com.hieu.ecommerce.model.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductVariantRequest {

    // ID của variant (null nếu là variant mới)
    private Long id;

    @NotBlank(message = "SKU không được để trống")
    private String sku;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    // Danh sách attribute values
    @NotEmpty(message = "Variant phải có ít nhất một attribute")
    private List<UpdateAttributeValueRequest> attributeValues;

    // Danh sách ID ảnh muốn giữ lại
    private List<Long> keepImageIds;

    // Danh sách ảnh mới muốn thêm
    private List<UpdateProductImageRequest> newImages;
}