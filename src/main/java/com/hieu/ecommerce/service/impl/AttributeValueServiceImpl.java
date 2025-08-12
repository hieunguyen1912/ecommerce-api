package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.model.dto.request.CreateAttributeValueRequest;
import com.hieu.ecommerce.model.dto.request.UpdateAttributeValueRequest;
import com.hieu.ecommerce.model.entity.Attribute;
import com.hieu.ecommerce.model.entity.AttributeValue;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.repository.AttributeRepository;
import com.hieu.ecommerce.repository.AttributeValueRepository;
import com.hieu.ecommerce.service.AttributeValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttributeValueServiceImpl implements AttributeValueService {
    private static final Logger logger = LoggerFactory.getLogger(AttributeValueServiceImpl.class);

    // Constants for validation messages
    private static final String REQUEST_NULL = "Attribute value request cannot be null";
    private static final String ATTRIBUTE_ID_NULL = "Attribute ID cannot be null";
    private static final String VALUE_NULL_OR_EMPTY = "Attribute value cannot be null or empty";
    private static final String ATTRIBUTE_NOT_FOUND = "Attribute not found: %d";

    private final AttributeValueRepository attributeValueRepository;
    private final AttributeRepository attributeRepository;

    public AttributeValueServiceImpl(AttributeValueRepository attributeValueRepository, AttributeRepository attributeRepository) {
        this.attributeValueRepository = attributeValueRepository;
        this.attributeRepository = attributeRepository;
    }

    @Override
    public AttributeValue createAttributeValue(CreateAttributeValueRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_NULL);
        }
        if (request.getAttributeId() == null) {
            throw new IllegalArgumentException(ATTRIBUTE_ID_NULL);
        }
        if (request.getValue() == null || request.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException(VALUE_NULL_OR_EMPTY);
        }
        
        logger.debug("Creating attribute value for attribute ID: {} with value: {}", 
                    request.getAttributeId(), request.getValue());
        
        return findOrCreate(request.getAttributeId(), request.getValue());
    }

    @Override
    public AttributeValue findOrCreate(Long attributeId, String value) {
        if (attributeId == null) {
            throw new IllegalArgumentException(ATTRIBUTE_ID_NULL);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(VALUE_NULL_OR_EMPTY);
        }
        
        String trimmedValue = value.trim();
        logger.debug("Finding or creating attribute value for attribute ID: {} with value: {}", 
                    attributeId, trimmedValue);
        
        return attributeValueRepository
                .findByAttributeIdAndValue(attributeId, trimmedValue)
                .orElseGet(() -> createNewAttributeValue(attributeId, trimmedValue));
    }

    private AttributeValue createNewAttributeValue(Long attributeId, String value) {
        logger.debug("Creating new attribute value for attribute ID: {} with value: {}", attributeId, value);
        
        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ATTRIBUTE_NOT_FOUND, attributeId)));
        
        AttributeValue newAttributeValue = new AttributeValue();
        newAttributeValue.setAttribute(attribute);
        newAttributeValue.setValue(value);
        
        AttributeValue savedValue = attributeValueRepository.save(newAttributeValue);
        logger.debug("Successfully created attribute value with ID: {} for attribute: {}", 
                    savedValue.getId(), attribute.getName());
        
        return savedValue;
    }

    @Override
    public List<AttributeValue> findOrCreateAll(List<UpdateAttributeValueRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return List.of();
        }
        return requests.stream()
                .map(req -> findOrCreate(req.getAttributeId(), req.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void replaceVariantAttributeValues(ProductVariant variant, List<UpdateAttributeValueRequest> requests) {
        logger.debug("Replacing attribute values for variant: {}", variant.getSku());
        if (!CollectionUtils.isEmpty(variant.getAttributeValues())) {
            int removedCount = variant.getAttributeValues().size();
            variant.getAttributeValues().clear();
            logger.debug("Cleared {} existing attribute values from variant: {}", removedCount, variant.getSku());
        }
        List<AttributeValue> newValues = findOrCreateAll(requests);
        variant.getAttributeValues().addAll(newValues);
        logger.debug("Added {} new attribute values to variant: {}", newValues.size(), variant.getSku());
    }
}
