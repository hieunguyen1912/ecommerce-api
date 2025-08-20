package com.hieu.ecommerce.model.dto.request;

import com.hieu.ecommerce.common.annotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopUpdateRequestDTO {
    @Size(max = 100, message = "Shop name must be at most 100 characters")
    private String shopName;

    private String description;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    @PhoneNumber(message = "Phone number must be valid")
    private String phoneNumber;

    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Email(message = "Email must be valid")
    private String email;

    private String logoUrl;
}
