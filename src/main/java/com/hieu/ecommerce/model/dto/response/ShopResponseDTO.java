package com.hieu.ecommerce.model.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopResponseDTO {
    private Long id;
    private String shopName;
    private String description;
    private String ownerName;
    private String createdAt;
    private Instant updatedAt;

}
