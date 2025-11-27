package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CancelOrderRequest;
import com.hieu.ecommerce.model.dto.request.CreateOrderRequest;
import com.hieu.ecommerce.model.dto.request.UpdateOrderStatusRequest;
import com.hieu.ecommerce.model.dto.response.OrderResponse;
import com.hieu.ecommerce.model.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrderFromCart(CreateOrderRequest request, String idempotencyKey);
    
    Page<OrderSummaryResponse> getOrderHistory(Pageable pageable);
    
    OrderResponse getOrderById(Long orderId);
    
    OrderResponse cancelOrder(Long orderId, CancelOrderRequest request);
    
    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
