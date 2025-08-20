package com.hieu.ecommerce.model.entity;

import com.hieu.ecommerce.common.annotation.EnumPattern;
import com.hieu.ecommerce.common.constant.ShopStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Shop extends Auditable{
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100, unique = true)
    private String shopName;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @EnumPattern(name = "Shop status", regexp = "^(ACTIVE|INACTIVE|DELETED)$", message = "Shop status must be one of: ACTIVE, INACTIVE, DELETED")
    @Column(nullable = false)
    private ShopStatus status = ShopStatus.ACTIVE;

    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Product> products = new ArrayList<>();
}
