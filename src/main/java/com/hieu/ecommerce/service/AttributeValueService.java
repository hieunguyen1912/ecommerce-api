package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.CreateAttributeValueRequest;
import com.hieu.ecommerce.model.dto.request.UpdateAttributeValueRequest;
import com.hieu.ecommerce.model.entity.AttributeValue;
import com.hieu.ecommerce.model.entity.ProductVariant;

import java.util.List;

public interface AttributeValueService {
    AttributeValue createAttributeValue(CreateAttributeValueRequest request);

    AttributeValue findOrCreate(Long attributeId, String value);

    List<AttributeValue> findOrCreateAll(List<UpdateAttributeValueRequest> requests);

    void replaceVariantAttributeValues(ProductVariant variant, List<UpdateAttributeValueRequest> requests);
}
