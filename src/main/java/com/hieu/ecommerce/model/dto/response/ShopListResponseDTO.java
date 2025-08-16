package com.hieu.ecommerce.model.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopListResponseDTO {
    private Long id;
    private String shopName;
    private String description;
    private String ownerName;
}
