package com.hieu.ecommerce.model.dto.response;

import com.hieu.ecommerce.constant.PaymentMethod;
import com.hieu.ecommerce.constant.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private Long orderId;
    private String orderNumber;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String currency;
    private String gatewayName;
    private String paymentUrl;
    private String gatewayTransactionId;
    private Instant createdAt;
    private Instant paidAt;
    private String failureReason;
}
