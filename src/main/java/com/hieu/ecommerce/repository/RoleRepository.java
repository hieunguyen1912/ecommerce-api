package com.hieu.ecommerce.repository;

import com.hieu.ecommerce.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
