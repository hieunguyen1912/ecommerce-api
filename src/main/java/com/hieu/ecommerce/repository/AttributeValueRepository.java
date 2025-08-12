package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    List<AttributeValue> findByIdIn(List<Long> ids);

    @Query("SELECT av FROM AttributeValue av WHERE av.attribute.id = :attributeId AND av.value = :value")
    Optional<AttributeValue> findByAttributeIdAndValue(@Param("attributeId") Long attributeId, @Param("value") String value);
} 