package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAttributeValueRequest {

    // ID của attribute value (null nếu là mới)
    private Long id;

    @NotNull(message = "Attribute ID không được null")
    private Long attributeId;

    @NotBlank(message = "Giá trị attribute không được để trống")
    private String value;
}