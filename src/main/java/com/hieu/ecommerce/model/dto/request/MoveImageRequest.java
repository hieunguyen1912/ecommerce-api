package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveImageRequest {
    
    @NotNull(message = "New product ID is required")
    private Long newProductId;
    
    @NotNull(message = "New variant ID is required")
    private Long newVariantId;
}

