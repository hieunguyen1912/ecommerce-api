package com.hieu.ecommerce.model.dto.request;

import com.hieu.ecommerce.annotation.EnumPattern;
import com.hieu.ecommerce.constant.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Order status is required")
        @EnumPattern(name = "Order Status", regexp = "^(PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED)$",
        message = "Order status must be one of: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED")
    private OrderStatus status;
    
    private String trackingNumber;
    
    private String note;
}

