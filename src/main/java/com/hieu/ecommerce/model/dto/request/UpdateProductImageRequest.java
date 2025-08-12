package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProductImageRequest {

    @NotBlank(message = "URL ảnh không được để trống")
    private String imageUrl;

    private boolean isDefault = false;
}