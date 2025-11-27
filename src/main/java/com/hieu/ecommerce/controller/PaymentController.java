package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.ProcessPaymentRequest;
import com.hieu.ecommerce.model.dto.response.PaymentResponse;
import com.hieu.ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}/initiate")
    @ResponseMessage("Payment initiated successfully")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @PathVariable Long orderId,
            @Valid @RequestBody ProcessPaymentRequest request,
            HttpServletRequest httpRequest) {

        if (request.getIpAddress() == null || request.getIpAddress().isEmpty()) {
            String clientIp = getClientIpAddress(httpRequest);
            request.setIpAddress(clientIp);
        }
        
        PaymentResponse response = paymentService.initiatePayment(orderId, request);
        
        log.info("Payment initiated successfully: {} for order: {}", 
            response.getPaymentNumber(), orderId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback/vnpay")
    public ResponseEntity<?> handleVnPayReturnUrl(
            @RequestParam Map<String, String> allParams) {
        
        log.info("Received VnPay RETURN URL callback: {}", allParams);
        
        String paymentNumber = allParams.get("vnp_TxnRef");
        if (paymentNumber == null || paymentNumber.isEmpty()) {
            log.error("Missing payment number in RETURN URL callback");
            String frontendUrl = "http://localhost:3000/payment/result?error=missing_payment_number";
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendUrl))
                .build();
        }
        
        PaymentResponse paymentResponse = paymentService
            .verifyPaymentCallback(paymentNumber, allParams);

        String frontendUrl = "http://localhost:3000/payment/result?" +
            "paymentNumber=" + paymentNumber + "&" +
            "status=" + paymentResponse.getPaymentStatus();

        log.info("RETURN URL verified successfully for payment: {} - Status: {}",
            paymentNumber, paymentResponse.getPaymentStatus());

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(frontendUrl))
            .build();
    }

    @PostMapping("/webhook/vnpay")
    public ResponseEntity<String> handleVnPayIpn(
            @RequestParam Map<String, String> allParams) {
        
        log.info("Received VnPay IPN (Webhook): {}", allParams);
        
        String paymentNumber = allParams.get("vnp_TxnRef");
        if (paymentNumber == null || paymentNumber.isEmpty()) {
            log.error("Missing payment number in IPN");
            return ResponseEntity.badRequest().body("Missing payment number");
        }
        
        PaymentResponse paymentResponse = paymentService
            .processPaymentCallback(paymentNumber, allParams);
        
        log.info("IPN processed successfully: {} - Status: {}", 
            paymentNumber, paymentResponse.getPaymentStatus());
        
        return ResponseEntity.ok("OK");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
