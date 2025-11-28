package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.model.entity.OrderItem;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.StockReservation;

import java.util.List;

public interface StockReservationService {

    List<StockReservation> reserveStockForOrder(Order order, List<OrderItem> orderItems, int reservationTimeoutMinutes);

    void confirmStockReservation(Order order);

    void releaseStockReservation(Order order, String reason);

    Integer getAvailableStock(ProductVariant productVariant);

    boolean hasEnoughStock(ProductVariant productVariant, Integer quantity);
}
