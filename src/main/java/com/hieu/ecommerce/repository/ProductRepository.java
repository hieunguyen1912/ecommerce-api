package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    Page<Product> findAllByShopId(Long shopId, Pageable pageable);

    Page<Product> findAllByShopIdAndStatusNot(Long shopId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndShopId(Long id, Long shopId);

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    @Modifying
    @Query("UPDATE Product p SET p.status = :status WHERE p.shop.id = :shopId")
    void updateStatusByShop(@Param("shopId") Long shopId, @Param("status") ProductStatus status);
}
