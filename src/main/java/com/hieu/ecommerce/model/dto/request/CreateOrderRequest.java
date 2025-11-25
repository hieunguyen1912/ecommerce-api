package com.hieu.ecommerce.model.dto.request;

import com.hieu.ecommerce.annotation.EnumPattern;
import com.hieu.ecommerce.constant.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String receiverPhone;

    @EnumPattern(name = "Payment Method", regexp = "^(COD|CREDIT_CARD|PAYPAL)$", message = "Method must be one of: COD, CREDIT_CARD, PAYPAL")
    private PaymentMethod paymentMethod;

    private String note;
    
    private String couponCode;
    
    private String idempotencyKey;
}
