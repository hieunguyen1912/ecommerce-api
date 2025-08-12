package com.hieu.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "attribute_values")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @ManyToMany(mappedBy = "attributeValues", fetch = FetchType.LAZY)
    private List<ProductVariant> productVariant;

    @Column(nullable = false)
    private String value;


}
