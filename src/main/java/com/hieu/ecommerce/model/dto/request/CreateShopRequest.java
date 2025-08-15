package com.hieu.ecommerce.model.dto.request;

import com.hieu.ecommerce.common.anotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateShopRequest {
    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must not exceed 100 characters")
    private String shopName;

    private String description;

    private String address;

    @NotBlank(message = "Phone number is required")
    @PhoneNumber(message = "Phone number must be valid")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Email(message = "Email must be valid")
    private String email;

    private String logoUrl;
}
