package com.hieu.ecommerce.model.dto.request;

import com.hieu.ecommerce.constant.OrderStatus;
import com.hieu.ecommerce.constant.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderFilterRequest {

    private List<OrderStatus> orderStatuses;
    private List<PaymentStatus> paymentStatuses;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDate;
    private LocalDate endDate;
}

