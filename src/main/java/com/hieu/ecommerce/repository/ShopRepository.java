package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByEmail(String email);
    boolean existsByShopName(String name);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByUserId(Long userId);
}
