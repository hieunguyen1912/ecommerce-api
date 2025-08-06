package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
