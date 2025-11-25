package com.hieu.ecommerce.constant;

public enum IdempotencyStatus {
    RESERVED,    // Key đã được reserve, đang processing
    PROCESSED,   // Đã xử lý thành công
    FAILED,      // Processing failed
    EXPIRED
}
