package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.model.dto.request.OrderFilterRequest;
import com.hieu.ecommerce.model.dto.response.OrderSummaryResponse;
import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.repository.OrderRepository;
import com.hieu.ecommerce.service.OrderExportService;
import com.hieu.ecommerce.specification.SearchOperation;
import com.hieu.ecommerce.specification.SpecificationsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExportServiceImpl implements OrderExportService {

    private final OrderRepository orderRepository;

    @Override
    public Resource exportOrdersToCsv(OrderFilterRequest filter) {
        if (filter == null) {
            filter = OrderFilterRequest.builder().build();
        }
        
        List<OrderSummaryResponse> allOrders = fetchAllOrders(filter);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        
        writer.println("Order Number,Status,Payment Status,Final Amount,Item Count,Created Date");
        
        for (OrderSummaryResponse order : allOrders) {
            writer.printf("%s,%s,%s,%.2f,%d,%s%n",
                    escapeCsvField(order.getOrderNumber()),
                    order.getOrderStatus() != null ? order.getOrderStatus().name() : "",
                    order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "",
                    order.getFinalAmount() != null ? order.getFinalAmount() : 0.0,
                    order.getItemCount() != null ? order.getItemCount() : 0,
                    order.getCreatedAt() != null ? order.getCreatedAt().toString() : ""
            );
        }
        
        writer.flush();
        writer.close();
        
        byte[] csvBytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvBytes);
        
        return new org.springframework.core.io.InputStreamResource(inputStream) {
            @Override
            public String getFilename() {
                return "orders_export_" + Instant.now().toEpochMilli() + ".csv";
            }
        };
    }

    private List<OrderSummaryResponse> fetchAllOrders(OrderFilterRequest filter) {
        List<OrderSummaryResponse> allOrders = new ArrayList<>();
        int pageSize = 1000;
        int pageNumber = 0;
        
        SpecificationsBuilder<Order> builder = buildSpecification(filter);
        Specification<Order> filterSpec = builder.build();
        
        Page<Order> page;
        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            page = filterSpec != null
                    ? orderRepository.findAll(filterSpec, pageable)
                    : orderRepository.findAll(pageable);
            
            // Convert to summary responses
            List<OrderSummaryResponse> orders = page.getContent().stream()
                    .map(this::toOrderSummaryResponse)
                    .toList();
            allOrders.addAll(orders);
            
            pageNumber++;
        } while (page.hasNext());
        
        return allOrders;
    }

    private SpecificationsBuilder<Order> buildSpecification(OrderFilterRequest filter) {
        SpecificationsBuilder<Order> builder = new SpecificationsBuilder<>();

        if (filter.getOrderStatuses() != null && !filter.getOrderStatuses().isEmpty()) {
            if (filter.getOrderStatuses().size() == 1) {
                builder.with("orderStatus", SearchOperation.EQUALITY, filter.getOrderStatuses().get(0));
            } else {
                builder.with("orderStatus", SearchOperation.IN, filter.getOrderStatuses());
            }
        }

        if (filter.getPaymentStatuses() != null && !filter.getPaymentStatuses().isEmpty()) {
            if (filter.getPaymentStatuses().size() == 1) {
                builder.with("paymentStatus", SearchOperation.EQUALITY, filter.getPaymentStatuses().get(0));
            } else {
                builder.with("paymentStatus", SearchOperation.IN, filter.getPaymentStatuses());
            }
        }

        if (filter.getMinAmount() != null) {
            builder.with("finalAmount", SearchOperation.GREATER_THAN_OR_EQUAL, filter.getMinAmount());
        }

        if (filter.getMaxAmount() != null) {
            builder.with("finalAmount", SearchOperation.LESS_THAN_OR_EQUAL, filter.getMaxAmount());
        }

        if (filter.getStartDate() != null) {
            Instant startInstant = filter.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            builder.with("createdAt", SearchOperation.GREATER_THAN_OR_EQUAL, startInstant);
        }

        if (filter.getEndDate() != null) {
            Instant endInstant = filter.getEndDate().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
            builder.with("createdAt", SearchOperation.LESS_THAN_OR_EQUAL, endInstant);
        }

        return builder;
    }

    private OrderSummaryResponse toOrderSummaryResponse(Order order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setFinalAmount(order.getFinalAmount());
        response.setItemCount(order.getItems() != null ? order.getItems().size() : 0);
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}

