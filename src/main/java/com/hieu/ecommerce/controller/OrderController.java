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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Orders", description = "API endpoints for order management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place order", description = "Create a new order from cart. Requires Idempotency-Key header")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or cart is empty")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Place order successfully")
    public ResponseEntity<OrderResponse> placeOrder(
        @Valid @RequestBody CreateOrderRequest request,
        @Parameter(description = "Idempotency key for preventing duplicate orders", required = true)
        @RequestHeader("Idempotency-Key") String idempotencyKey) {
        OrderResponse response = orderService.placeOrderFromCart(request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Retrieve paginated order history for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order history retrieved successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get order history successfully")
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOrderHistory(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<OrderSummaryResponse> orders = orderService.getOrderHistory(pageable);
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Get order details successfully")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Cancel order successfully")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderResponse response = orderService.cancelOrder(orderId, request);
        return ResponseEntity.ok(response);
    }
}
