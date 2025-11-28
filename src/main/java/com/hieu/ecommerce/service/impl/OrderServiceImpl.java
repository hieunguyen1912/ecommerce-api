package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.annotation.Idempotent;
import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.OrderStatus;
import com.hieu.ecommerce.constant.PaymentStatus;
import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.constant.VariantStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.model.dto.request.CancelOrderRequest;
import com.hieu.ecommerce.model.dto.request.CreateOrderRequest;
import com.hieu.ecommerce.model.dto.request.OrderFilterRequest;
import com.hieu.ecommerce.model.dto.request.UpdateOrderStatusRequest;
import com.hieu.ecommerce.model.dto.response.OrderResponse;
import com.hieu.ecommerce.model.dto.response.OrderSummaryResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.mapper.OrderMapper;
import com.hieu.ecommerce.repository.*;
import com.hieu.ecommerce.service.CartService;
import com.hieu.ecommerce.service.OrderService;
import com.hieu.ecommerce.service.StockReservationService;
import com.hieu.ecommerce.util.OrderNumberGenerator;
import com.hieu.ecommerce.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hieu.ecommerce.specification.SearchOperation;
import com.hieu.ecommerce.specification.SpecificationsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final StockReservationService stockReservationService;

    @Override
    @Transactional
    @Idempotent
    public OrderResponse placeOrderFromCart(CreateOrderRequest request, String idempotencyKey) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Cart is empty"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_OPERATION, "Cart is empty");
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    ProductVariant variant = cartItem.getProductVariant();

                    if (product.getStatus() != ProductStatus.ACTIVE) {
                        throw new AppException(ErrorCode.INVALID_OPERATION,
                                "Product " + product.getName() + " is not available");
                    }

                    if (variant.getStatus() != VariantStatus.ACTIVE) {
                        throw new AppException(ErrorCode.INVALID_OPERATION,
                                "Product variant " + variant.getSku() + " is not available");
                    }

                    if (!stockReservationService.hasEnoughStock(variant, cartItem.getQuantity())) {
                        throw new AppException(ErrorCode.INVALID_OPERATION,
                                "Insufficient stock for " + product.getName() + " - " + variant.getSku() +
                                        ". Available: " + variant.getStock() + ", Requested: " + cartItem.getQuantity());
                    }

                    BigDecimal unitPrice = variant.getPrice();
                    BigDecimal itemSubTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(product);
                    orderItem.setProductVariant(variant);
                    orderItem.setProductName(product.getName());
                    orderItem.setSku(variant.getSku());
                    orderItem.setUnitPrice(unitPrice);
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setDiscountAmount(BigDecimal.ZERO);
                    orderItem.setSubTotal(itemSubTotal);

                    String productImage = getProductImage(variant);
                    orderItem.setProductImage(productImage);

                    return orderItem;
                })
                .toList();

            totalAmount = orderItems.stream()
                    .map(OrderItem::getSubTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order();
            order.setUser(user);
            order.setOrderNumber(OrderNumberGenerator.generateOrderNumber());
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setShippingAddress(request.getShippingAddress());
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setNote(request.getNote());
            order.setCouponCode(request.getCouponCode());
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setShippingFee(BigDecimal.ZERO);
            order.setTaxAmount(BigDecimal.ZERO);
            order.setFinalAmount(totalAmount);

            Order savedOrder = orderRepository.save(order);

            orderItems.forEach(item -> {
                item.setOrder(savedOrder);
                orderItemRepository.save(item);
            });

            stockReservationService.reserveStockForOrder(order, orderItems, 15);

            savedOrder.setItems(orderItems);

            cartService.clearCart();

            return orderMapper.toOrderResponse(savedOrder);
    }

    private String getProductImage(ProductVariant variant) {
        List<ProductImage> images = variant.getImages();
        if (images != null && !images.isEmpty()) {
            ProductImage thumbnail = images.stream()
                    .filter(ProductImage::isThumbnail)
                    .findFirst()
                    .orElse(images.get(0));
            return thumbnail.getImageUrl();
        }
        
        List<ProductImage> productImages = productImageRepository.findAllByProductId(variant.getProduct().getId());
        if (productImages != null && !productImages.isEmpty()) {
            ProductImage thumbnail = productImages.stream()
                    .filter(ProductImage::isThumbnail)
                    .findFirst()
                    .orElse(productImages.get(0));
            return thumbnail.getImageUrl();
        }
        
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrderHistory(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        Page<Order> orders = orderRepository.findByUser(user, pageable);
        
        log.info("Retrieved {} orders for user: {}", orders.getTotalElements(), userId);
        
        return orders.map(orderMapper::toOrderSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                    "Order not found or does not belong to user"));

        log.info("Retrieved order: {} for user: {}", order.getOrderNumber(), userId);
        
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                    "Order not found or does not belong to user"));

        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_OPERATION, 
                "Cannot cancel order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason(request.getReason());

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                order.getItems().forEach(item -> {
                    ProductVariant variant = item.getProductVariant();
                    variant.setStock(variant.getStock() + item.getQuantity());
                    productVariantRepository.save(variant);
                    log.debug("Restored stock for variant {}: +{} units (order was paid)", 
                        variant.getSku(), item.getQuantity());
                });
            }
            log.info("Restored stock for cancelled order {} (order was paid)", order.getOrderNumber());
        } else {
            stockReservationService.releaseStockReservation(order, 
                "Order cancelled by user: " + request.getReason());
            log.info("Released stock reservations for cancelled order {} (order was not paid)", 
                order.getOrderNumber());
        }

        Order savedOrder = orderRepository.save(order);
        
        log.info("Order {} cancelled by user: {}", savedOrder.getOrderNumber(), userId);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Order not found"));

        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus newStatus = request.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        order.setOrderStatus(newStatus);

        switch (newStatus) {
            case CONFIRMED:
                break;
            case SHIPPED:
                order.setShippedAt(Instant.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(Instant.now());
                break;
            case CANCELLED:
                order.setCancelledAt(Instant.now());
                if (request.getNote() != null && !request.getNote().isEmpty()) {
                    order.setCancellationReason(request.getNote());
                }
                if (order.getPaymentStatus() == PaymentStatus.PAID) {
                    if (order.getItems() != null && !order.getItems().isEmpty()) {
                        order.getItems().forEach(item -> {
                            ProductVariant variant = item.getProductVariant();
                            variant.setStock(variant.getStock() + item.getQuantity());
                            productVariantRepository.save(variant);
                            log.debug("Restored stock for variant {}: +{} units (order was paid)", 
                                variant.getSku(), item.getQuantity());
                        });
                    }
                    log.info("Restored stock for cancelled order {} (order was paid)", order.getOrderNumber());
                } else {
                    stockReservationService.releaseStockReservation(order, 
                        "Order cancelled by admin: " + (request.getNote() != null ? request.getNote() : "No reason"));
                    log.info("Released stock reservations for cancelled order {} (order was not paid)", 
                        order.getOrderNumber());
                }
                break;
            case PENDING:
                break;
        }

        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isEmpty()) {
            order.setTrackingNumber(request.getTrackingNumber());
            if (order.getShippedAt() == null && newStatus != OrderStatus.CANCELLED) {
                order.setShippedAt(Instant.now());
            }
        }

        if (request.getNote() != null && !request.getNote().isEmpty() && newStatus != OrderStatus.CANCELLED) {
            order.setNote(request.getNote());
        }

        Order savedOrder = orderRepository.save(order);
        
        log.info("Order {} status updated from {} to {} by admin", 
            savedOrder.getOrderNumber(), currentStatus, newStatus);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrdersForAdmin(Pageable pageable, OrderFilterRequest filter) {
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

        Specification<Order> filterSpec = builder.build();

        Page<Order> orders = filterSpec != null
                ? orderRepository.findAll(filterSpec, pageable)
                : orderRepository.findAll(pageable);

        log.info("Retrieved {} orders for admin", orders.getTotalElements());

        return orders.map(orderMapper::toOrderSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Order with id " + orderId + " not found"));

        log.info("Retrieved order: {} for admin", order.getOrderNumber());

        return orderMapper.toOrderResponse(order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus == OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_OPERATION, 
                "Cannot change status from DELIVERED");
        }

        if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_OPERATION, 
                "Cannot change status from CANCELLED to " + newStatus);
        }

        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Cannot change status from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.CANCELLED) {
                    throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Cannot change status from CONFIRMED to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Cannot change status from SHIPPED to " + newStatus);
                }
                break;
            case DELIVERED:
                throw new AppException(ErrorCode.INVALID_OPERATION, 
                    "Cannot change status from DELIVERED");
            case CANCELLED:
                if (newStatus != OrderStatus.PENDING) {
                    throw new AppException(ErrorCode.INVALID_OPERATION, 
                        "Cannot change status from CANCELLED to " + newStatus);
                }
                break;
        }
    }
}
