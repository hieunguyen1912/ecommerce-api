package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeValueRequest {
    @NotBlank(message = "Attribute id cannot be blank")
    private Long attributeId;

    @NotBlank(message = "Attribute value cannot be blank")
    @Size(max = 50, message = "Attribute value must not exceed 50 characters")
    private String value;
}
