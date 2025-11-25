package com.hieu.ecommerce.constant;

public enum OrderStatus {
    PENDING,      // Chờ xử lý
    CONFIRMED,    // Đã xác nhận
    SHIPPED,      // Đã gửi hàng (đang vận chuyển)
    DELIVERED,    // Đã giao hàng (đã đến tay khách)
    CANCELLED     // Đã hủy
}
