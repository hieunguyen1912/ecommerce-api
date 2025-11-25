package com.hieu.ecommerce.model.dto.response;

import com.hieu.ecommerce.constant.OrderStatus;
import com.hieu.ecommerce.constant.PaymentMethod;
import com.hieu.ecommerce.constant.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String note;
    private String couponCode;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private String trackingNumber;
    private Instant createdAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private List<OrderItemResponse> items;
}
