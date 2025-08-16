package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.common.enums.ShopStatus;
import com.hieu.ecommerce.model.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByEmail(String email);
    boolean existsByShopName(String name);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByUserId(Long userId);
    boolean existsByShopNameAndIdNot(String shopName, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    Page<Shop> findAllByStatus(ShopStatus status, Pageable pageable);
    Optional<Shop> findByIdAndStatus(Long id, ShopStatus status);
}
