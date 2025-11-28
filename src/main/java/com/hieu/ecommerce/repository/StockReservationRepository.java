package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.constant.ReservationStatus;
import com.hieu.ecommerce.model.entity.Order;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Integer> {

    @Query("SELECT COALESCE(SUM(sr.quantity), 0) FROM StockReservation sr " +
            "WHERE sr.productVariant = :variant AND sr.status = :status")
    Integer calculateReservedQuantity(
            @Param("variant") ProductVariant variant,
            @Param("status") ReservationStatus status
    );

    List<StockReservation> findByOrderAndStatus(Order order, ReservationStatus status);
}
