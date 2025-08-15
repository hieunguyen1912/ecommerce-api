package com.hieu.ecommerce.model.dto.response;

import com.hieu.ecommerce.common.enums.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private String shopName;
    private String description;
    private String address;
    private String phoneNumber;
    private String email;
    private String logoUrl;
    private ShopStatus status;
    private String ownerName;
}
