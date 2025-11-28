package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.PaymentStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.PaymentMapper;
import com.hieu.ecommerce.model.dto.request.ProcessPaymentRequest;
import com.hieu.ecommerce.model.dto.response.PaymentResponse;
import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.model.entity.Payment;
import com.hieu.ecommerce.repository.OrderRepository;
import com.hieu.ecommerce.repository.PaymentRepository;
import com.hieu.ecommerce.service.PaymentGateway;
import com.hieu.ecommerce.service.PaymentService;
import com.hieu.ecommerce.service.StockReservationService;
import com.hieu.ecommerce.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final List<PaymentGateway> paymentGateways;
    private final PaymentMapper paymentMapper;
    private final StockReservationService stockReservationService;

    @Override
    public PaymentResponse initiatePayment(Long orderId, ProcessPaymentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Order not found or does not belong to user"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_OPERATION,
                    "Order already paid");
        }

        PaymentGateway paymentGateway = findGateway(order.getPaymentMethod().name());

        Payment payment = paymentGateway.initiatePayment(order, request);

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse verifyPaymentCallback(String paymentNumber, Object callbackData) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Payment not found: " + paymentNumber));

        PaymentGateway gateway = findGateway(payment.getPaymentMethod().name());

        if (gateway.verifyCallbackSignature(callbackData)) {
            log.error("Invalid signature for payment: {}", paymentNumber);
            throw new AppException(ErrorCode.INVALID_OPERATION,
                    "Invalid signature");
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse processPaymentCallback(String paymentNumber, Object callbackData) {

        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                return paymentMapper.toResponse(payment);
        }

        PaymentGateway paymentGateway = findGateway(payment.getPaymentMethod().name());
        Payment updatedPayment = paymentGateway.processCallback(payment, callbackData);
        Payment savedPayment = paymentRepository.save(updatedPayment);

        Order order = orderRepository.findById(savedPayment.getOrder().getId())
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                "Order not found: " + savedPayment.getOrder().getId()));
        
        order.setPaymentStatus(savedPayment.getPaymentStatus());
        orderRepository.save(order);

        if (savedPayment.getPaymentStatus() == PaymentStatus.PAID) {
            stockReservationService.confirmStockReservation(order);
            log.info("Stock reservations confirmed for order {} (payment successful)", 
                order.getOrderNumber());
        } else if (savedPayment.getPaymentStatus() == PaymentStatus.FAILED) {
            stockReservationService.releaseStockReservation(order, 
                "Payment failed: " + (savedPayment.getFailureReason() != null ? 
                    savedPayment.getFailureReason() : "Unknown reason"));
            log.info("Stock reservations released for order {} (payment failed)", 
                order.getOrderNumber());
        }

        return paymentMapper.toResponse(savedPayment);
    }

    private PaymentGateway findGateway(String paymentMethod) {
        return paymentGateways.stream()
                .filter(gateway -> gateway.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OPERATION,
                        "Payment gateway not found for method: " + paymentMethod));
    }

}
