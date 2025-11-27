package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.PaymentMethod;
import com.hieu.ecommerce.constant.PaymentStatus;
import com.hieu.ecommerce.model.dto.request.ProcessPaymentRequest;
import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.model.entity.Payment;
import com.hieu.ecommerce.service.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VnPayGateway implements PaymentGateway {

    @Value("${payment.vnpay.vnp_TmnCode}")
    private String tmnCode;

    @Value("${payment.vnpay.return_Url}")
    private String returnUrl;

    @Value("${payment.vnpay.vnp_HashSecret}")
    private String secretKey;

    @Value("${payment.vnpay.vnp_Url}")
    private String vnpayUrl;

    @Value("${payment.vnpay.ipn_Url}")
    private String ipnUrl;

    @Override
    public Payment initiatePayment(Order order, ProcessPaymentRequest request) {
        String paymentNumber = "PAY-" + order.getOrderNumber() + "-" + System.currentTimeMillis();

        long vnpAmount = order.getFinalAmount().longValue() * 100;

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", paymentNumber);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + paymentNumber);
        vnp_Params.put("vnp_OrderType", orderType);

        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpayUrl + "?" + queryUrl;

        return Payment.builder()
                .order(order)
                .paymentNumber(paymentNumber)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(order.getFinalAmount())
                .currency("VND")
                .gatewayName("VNPAY")
                .paymentUrl(paymentUrl)
                .build();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hash = hmac512.doFinal(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC", e);
        }
    }

    @Override
    public Payment processCallback(Payment payment, Object callbackData) {
        Map<String, String> vnpParams = (Map<String, String>) callbackData;

        if (!verifyCallbackSignature(callbackData)) {
            log.error("Invalid signature for payment: {}", payment.getPaymentNumber());
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid signature from VnPay");
            return payment;
        }

        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnpTransactionStatus = vnpParams.get("vnp_TransactionStatus");
        String vnpTransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnpPayDate = vnpParams.get("vnp_PayDate");

        if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setGatewayTransactionId(vnpTransactionNo);

            if (vnpPayDate != null && !vnpPayDate.isEmpty()) {
                try {
                    Instant paidAt = Instant.from(
                            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                                    .parse(vnpPayDate)
                    );
                    payment.setPaidAt(paidAt);
                } catch (Exception e) {
                    log.warn("Error parsing pay date: {}", vnpPayDate, e);
                    payment.setPaidAt(Instant.now());
                }
            } else {
                payment.setPaidAt(Instant.now());
            }

            log.info("Payment {} completed successfully. Transaction ID: {}",
                    payment.getPaymentNumber(), vnpTransactionNo);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            String errorMessage = getVnPayErrorMessage(vnpResponseCode);
            payment.setFailureReason("VnPay error: " + errorMessage +
                    " (ResponseCode: " + vnpResponseCode +
                    ", TransactionStatus: " + vnpTransactionStatus + ")");

            log.warn("Payment {} failed. ResponseCode: {}, TransactionStatus: {}, Error: {}",
                    payment.getPaymentNumber(), vnpResponseCode, vnpTransactionStatus, errorMessage);
        }

        try {
            String gatewayResponse = convertMapToJson(vnpParams);
            payment.setGatewayResponse(gatewayResponse);
        } catch (Exception e) {
            log.warn("Error converting gateway response to JSON", e);
        }

        return payment;
    }

    @Override
    public boolean verifyCallbackSignature(Object callbackData) {
        Map<String, String> vnpParams = (Map<String, String>) callbackData;
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");

        Map<String, String> paramsToVerify = new HashMap<>(vnpParams);
        paramsToVerify.remove("vnp_SecureHash");

        String queryString = paramsToVerify.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String calculatedHash = hmacSHA512(secretKey, queryString);

        return calculatedHash.equals(vnpSecureHash);
    }

    @Override
    public boolean supports(String paymentMethod) {
        return PaymentMethod.CREDIT_CARD.name().equals(paymentMethod);
    }

    private String getVnPayErrorMessage(String responseCode) {
        if (responseCode == null) {
            return "Unknown error";
        }
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "07":
                return "Trừ tiền thành công, giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Thẻ/Tài khoản chưa đăng ký dịch vụ Internet Banking";
            case "10":
                return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch";
            case "12":
                return "Thẻ/Tài khoản bị khóa";
            case "13":
                return "Nhập sai mật khẩu xác thực giao dịch (OTP) quá số lần quy định";
            case "51":
                return "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Tài khoản đã vượt quá hạn mức giao dịch cho phép";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Nhập sai mật khẩu thanh toán quá số lần quy định";
            default:
                return "Lỗi không xác định. Mã lỗi: " + responseCode;
        }
    }

    private String convertMapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}
