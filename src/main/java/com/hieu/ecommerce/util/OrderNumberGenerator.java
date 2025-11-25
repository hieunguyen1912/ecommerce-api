package com.hieu.ecommerce.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderNumberGenerator {
    
    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Generate unique order number in format: ORD-YYYYMMDD-XXXX
     * Example: ORD-20240115-0001
     */
    public static String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String timestampPart = String.valueOf(System.currentTimeMillis() % 10000);
        
        // Pad with zeros to ensure 4 digits
        timestampPart = String.format("%04d", Long.parseLong(timestampPart));
        
        return String.format("%s-%s-%s", PREFIX, datePart, timestampPart);
    }
    
    /**
     * Generate order number with sequence number
     * Format: ORD-YYYYMMDD-XXXX
     */
    public static String generateOrderNumber(long sequenceNumber) {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String sequencePart = String.format("%04d", sequenceNumber);
        
        return String.format("%s-%s-%s", PREFIX, datePart, sequencePart);
    }
}

