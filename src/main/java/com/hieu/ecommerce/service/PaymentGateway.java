package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.ProcessPaymentRequest;
import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.model.entity.Payment;

public interface PaymentGateway {
    Payment initiatePayment(Order order, ProcessPaymentRequest request);
    Payment processCallback(Payment payment, Object callbackData);
    boolean verifyCallbackSignature(Object callbackData);
    boolean supports(String paymentMethod);
}
