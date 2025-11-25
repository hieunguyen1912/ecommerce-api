package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.entity.AttributeValue;
import com.hieu.ecommerce.model.entity.ProductVariant;

import java.util.List;

public interface AttributeValueService {
    List<AttributeValue> findByIds(List<Long> attributeValueIds);

    void updateAttributeValues(ProductVariant variant, List<Long> attributeValueIds);
}
