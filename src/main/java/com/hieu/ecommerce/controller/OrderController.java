package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.Idempotent;
import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.CancelOrderRequest;
import com.hieu.ecommerce.model.dto.request.CreateOrderRequest;
import com.hieu.ecommerce.model.dto.response.OrderResponse;
import com.hieu.ecommerce.model.dto.response.OrderSummaryResponse;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.service.OrderService;
import com.hieu.ecommerce.util.PaginationHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseMessage("Place order successfully")
    public ResponseEntity<OrderResponse> placeOrder(
        @Valid @RequestBody CreateOrderRequest request,
        @RequestHeader("Idempotency-Key") String idempotencyKey) {
        OrderResponse response = orderService.placeOrderFromCart(request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @ResponseMessage("Get order history successfully")
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOrderHistory(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<OrderSummaryResponse> orders = orderService.getOrderHistory(pageable);
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(orders));
    }

    @GetMapping("/{orderId}")
    @ResponseMessage("Get order details successfully")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    @ResponseMessage("Cancel order successfully")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderResponse response = orderService.cancelOrder(orderId, request);
        return ResponseEntity.ok(response);
    }
}
