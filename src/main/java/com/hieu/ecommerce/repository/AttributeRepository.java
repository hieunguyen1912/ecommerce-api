package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    @Query("SELECT a FROM Attribute a WHERE a.id = :id")
    Optional<Attribute> findById(@Param("id") Long id);
}
