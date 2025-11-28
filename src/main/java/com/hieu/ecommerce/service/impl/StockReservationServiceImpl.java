package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.ReservationStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.model.entity.OrderItem;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.StockReservation;
import com.hieu.ecommerce.repository.ProductVariantRepository;
import com.hieu.ecommerce.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.hieu.ecommerce.service.StockReservationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockReservationServiceImpl implements StockReservationService {

    private final StockReservationRepository stockReservationRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public List<StockReservation> reserveStockForOrder(Order order, List<OrderItem> orderItems, int reservationTimeoutMinutes) {
        List<StockReservation> stockReservations = new ArrayList<>();
        Instant expiresAt = Instant.now().plus(reservationTimeoutMinutes, ChronoUnit.MINUTES);

        for (OrderItem orderItem : orderItems) {
            ProductVariant productVariant = orderItem.getProductVariant();

            productVariant = productVariantRepository.findById(productVariant.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            Integer availableStock = getAvailableStock(productVariant);

            if (orderItem.getQuantity() > availableStock) {
                throw new AppException(ErrorCode.INVALID_OPERATION,
                        String.format("Insufficient stock for %s - %s. Available: %d, Requested: %d",
                                orderItem.getProductName(), productVariant.getSku(),
                                availableStock, orderItem.getQuantity()));
            }

            StockReservation stockReservation = StockReservation.builder()
                    .order(order)
                    .quantity(orderItem.getQuantity())
                    .productVariant(productVariant)
                    .expiresAt(expiresAt)
                    .build();

            stockReservations.add(stockReservation);
            log.debug("Reserved {} units of variant {} for order {} (expires at {})");
        }
        stockReservationRepository.saveAll(stockReservations);
        log.info("Created {} stock reservations for order {}");
        return stockReservations;
    }

    @Override
    @Transactional
    public void confirmStockReservation(Order order) {
        List<StockReservation> stockReservations =
                stockReservationRepository.findByOrderAndStatus(order, ReservationStatus.ACTIVE);

        if (stockReservations.isEmpty()) {
            log.debug("No active reservations to confirm for order {}", order.getOrderNumber());
            return;
        }

        Instant confirmedAt = Instant.now();
        for (StockReservation stockReservation : stockReservations) {
            ProductVariant productVariant = productVariantRepository
                .findById(stockReservation.getProductVariant().getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product variant not found: " + stockReservation.getProductVariant().getId()));

            if (productVariant.getVersion() == null) {
                productVariant.setVersion(0L);
            }

            productVariant.setStock(productVariant.getStock() - stockReservation.getQuantity());
            productVariantRepository.save(productVariant);

            stockReservation.setStatus(ReservationStatus.CONFIRMED);
            stockReservation.setConfirmedAt(confirmedAt);
            stockReservationRepository.save(stockReservation);

            log.debug("Confirmed reservation {}: deducted {} units from variant {}",
                stockReservation.getId(), stockReservation.getQuantity(), productVariant.getSku());
        }

        log.info("Confirmed {} reservations for order {}", stockReservations.size(), order.getOrderNumber());
    }

    @Override
    @Transactional
    public void releaseStockReservation(Order order, String reason) {
        List<StockReservation> reservations = stockReservationRepository
                .findByOrderAndStatus(order, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            log.debug("No active reservations to release for order {}", order.getOrderNumber());
            return;
        }

        Instant releasedAt = Instant.now();
        for (StockReservation stockReservation : reservations) {
            stockReservation.setStatus(ReservationStatus.RELEASED);
            stockReservation.setReleasedAt(releasedAt);
            stockReservation.setReleaseReason(reason);
            stockReservationRepository.save(stockReservation);

            log.debug("Released reservation {}: {} units of variant {} - Reason: {}",
                    stockReservation.getId(), stockReservation.getQuantity(), 
                    stockReservation.getProductVariant().getSku(), reason);
        }

        log.info("Released {} reservations for order {}: {}", 
            reservations.size(), order.getOrderNumber(), reason);
    }

    @Override
    public Integer getAvailableStock(ProductVariant variant) {
        Integer actualStock = variant.getStock();

        Integer reservedStock = stockReservationRepository.calculateReservedQuantity(
                variant,
                ReservationStatus.ACTIVE
        );

        Integer availableStock = Math.max(0, actualStock - reservedStock);

        log.debug("Variant {} - Stock: {}, Reserved: {}, Available: {}",
                variant.getSku(), actualStock, reservedStock, availableStock);

        return availableStock;
    }

    @Override
    public boolean hasEnoughStock(ProductVariant productVariant, Integer quantity) {
        return getAvailableStock(productVariant) > quantity;
    }
}
