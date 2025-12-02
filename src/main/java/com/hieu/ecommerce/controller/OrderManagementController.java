package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.OrderFilterRequest;
import com.hieu.ecommerce.model.dto.request.UpdateOrderStatusRequest;
import com.hieu.ecommerce.model.dto.response.OrderResponse;
import com.hieu.ecommerce.model.dto.response.OrderSummaryResponse;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.service.OrderExportService;
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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "API endpoints for order management (Admin only)")
public class OrderManagementController {

    private final OrderService orderService;
    private final OrderExportService orderExportService;

    @GetMapping
    @Operation(summary = "Get all orders (Admin)", description = "Retrieve paginated list of all orders with filters. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Orders retrieved successfully")
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getAllOrders(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Valid @ModelAttribute OrderFilterRequest filter) {
        Page<OrderSummaryResponse> orders = orderService.getAllOrdersForAdmin(pageable, filter);
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID (Admin)", description = "Retrieve order details by ID. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Order retrieved successfully")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderByIdForAdmin(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order status. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Update order status successfully")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @Operation(summary = "Export orders", description = "Export orders to CSV file. Requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders exported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid format"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    @ResponseMessage("Orders exported successfully")
    public ResponseEntity<Resource> exportOrders(
            @Valid @ModelAttribute OrderFilterRequest filter,
            @Parameter(description = "Export format (csv)", example = "csv") @RequestParam(defaultValue = "csv") String format) {
        
        if (!"csv".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest().build();
        }
        
        Resource resource = orderExportService.exportOrdersToCsv(filter);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}

