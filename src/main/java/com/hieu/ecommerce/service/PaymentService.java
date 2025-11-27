package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.ProcessPaymentRequest;
import com.hieu.ecommerce.model.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse initiatePayment(Long orderId, ProcessPaymentRequest request);

    PaymentResponse verifyPaymentCallback(String paymentNumber, Object callbackData);

    PaymentResponse processPaymentCallback(String paymentNumber, Object callbackData);
}
