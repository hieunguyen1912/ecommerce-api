package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.RefreshToken;
import com.hieu.ecommerce.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUser(User user);
    
    List<RefreshToken> findByExpiryDateBefore(Instant expiryDate);
} 