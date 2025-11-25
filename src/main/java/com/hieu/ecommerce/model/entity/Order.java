package com.hieu.ecommerce.model.entity;

import com.hieu.ecommerce.constant.OrderStatus;
import com.hieu.ecommerce.constant.PaymentMethod;
import com.hieu.ecommerce.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mã đơn hàng duy nhất (ví dụ: ORD-20240101-0001)
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    // Thông tin giao hàng
    @Column(name = "shipping_address", columnDefinition = "TEXT", nullable = false)
    private String shippingAddress;

    @Column(name = "receiver_name", length = 100, nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20, nullable = false)
    private String receiverPhone;

    // Phương thức thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    // Trạng thái thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    // Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    // Ghi chú từ khách hàng
    @Column(columnDefinition = "TEXT")
    private String note;

    // Tổng tiền hàng (chưa bao gồm phí ship, thuế, giảm giá)
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    // Tổng giảm giá (từ coupon, voucher, etc.)
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Mã giảm giá (nếu có)
    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    // Phí vận chuyển
    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // Thuế VAT (nếu có)
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // Tổng cuối cùng (totalAmount - discountAmount + shippingFee + taxAmount)
    @Column(name = "final_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    // Mã vận đơn (tracking number)
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    // Thời gian các sự kiện
    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    // Lý do hủy đơn (nếu có)
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Key để tránh duplicate orders (idempotency)
    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    // Danh sách các sản phẩm trong đơn hàng
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
