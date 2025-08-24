package com.hieu.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private Long cartId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;    // Tổng tiền tạm tính (chỉ các sản phẩm còn available)
    private Integer totalItems;        // Tổng số lượng item trong giỏ
    private boolean hasInvalidItems;
}
