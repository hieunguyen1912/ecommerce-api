package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.ProcessPaymentRequest;
import com.hieu.ecommerce.model.dto.response.PaymentResponse;
import com.hieu.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "API endpoints for payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}/initiate")
    @Operation(summary = "Initiate payment", description = "Initiate payment for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Payment initiated successfully")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
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
    @Operation(summary = "VnPay return URL callback", description = "Handle VnPay payment return callback (for frontend redirect)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to frontend payment result page")
    })
    public ResponseEntity<?> handleVnPayReturnUrl(
            @Parameter(description = "VnPay callback parameters") @RequestParam Map<String, String> allParams) {
        
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
            .processPaymentCallback(paymentNumber, allParams);

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
    @Operation(summary = "VnPay IPN webhook", description = "Handle VnPay IPN (Instant Payment Notification) webhook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IPN processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid IPN data")
    })
    public ResponseEntity<String> handleVnPayIpn(
            @Parameter(description = "VnPay IPN parameters") @RequestParam Map<String, String> allParams) {
        
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
