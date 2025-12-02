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
public class OrderManagementController {

    private final OrderService orderService;
    private final OrderExportService orderExportService;

    @GetMapping
    @ResponseMessage("Orders retrieved successfully")
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getAllOrders(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Valid @ModelAttribute OrderFilterRequest filter) {
        Page<OrderSummaryResponse> orders = orderService.getAllOrdersForAdmin(pageable, filter);
        return ResponseEntity.ok(PaginationHelper.toPaginatedResponse(orders));
    }

    @GetMapping("/{orderId}")
    @ResponseMessage("Order retrieved successfully")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderByIdForAdmin(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    @ResponseMessage("Update order status successfully")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @ResponseMessage("Orders exported successfully")
    public ResponseEntity<Resource> exportOrders(
            @Valid @ModelAttribute OrderFilterRequest filter,
            @RequestParam(defaultValue = "csv") String format) {
        
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

