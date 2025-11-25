package com.hieu.ecommerce.service;


import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;

public interface InventoryReservationService {


    boolean checkAvailability(Product product, ProductVariant productVariant, Integer quantity);


    void reserveStock(Product product, ProductVariant productVariant, Integer quantity, String reservationReference);


    void releaseReservation(String reservationReference);


    void confirmReservation(String reservationReference);

    void addStock(Long productId, Long variantId, Integer quantity);
}
