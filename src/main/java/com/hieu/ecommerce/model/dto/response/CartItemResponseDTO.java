package com.hieu.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private Long cartItemId;

    private Long productId;
    private String productName;
    private String productImage;

    private Long productVariantId;
    private String variantName;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal priceSnapshot;
    private BigDecimal totalPrice;

    private String sellerName;
    private boolean available;
}
