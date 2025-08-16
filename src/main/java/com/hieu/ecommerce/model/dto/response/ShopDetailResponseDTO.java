package com.hieu.ecommerce.model.dto.response;

import com.hieu.ecommerce.common.enums.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopDetailResponseDTO  {
    private String shopName;
    private String description;
    private String address;
    private String phoneNumber;
    private String email;
    private String logoUrl;
    private ShopStatus status;
    private String ownerName;
    private List<ProductSummaryResponse> products;
}
