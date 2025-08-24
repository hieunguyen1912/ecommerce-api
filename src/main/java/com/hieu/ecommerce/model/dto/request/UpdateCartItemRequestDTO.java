package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequestDTO {
    @NotNull
    private Long cartItemId;

    @Min(1)
    private Integer quantity;
}
