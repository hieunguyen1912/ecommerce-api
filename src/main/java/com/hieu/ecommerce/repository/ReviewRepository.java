package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProduct_Id(Long productId, Pageable pageable);
}
