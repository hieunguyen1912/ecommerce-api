package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
