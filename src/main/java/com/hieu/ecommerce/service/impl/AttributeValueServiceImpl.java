package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.model.entity.AttributeValue;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.repository.AttributeValueRepository;
import com.hieu.ecommerce.service.AttributeValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AttributeValueServiceImpl implements AttributeValueService {

    private final AttributeValueRepository attributeValueRepository;

    @Override
    public List<AttributeValue> findByIds(List<Long> attributeValueIds) {
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            return new ArrayList<>();
        }
        return attributeValueRepository.findByIdIn(attributeValueIds);
    }

    @Override
    public void updateAttributeValues(ProductVariant variant, List<Long> attributeValueIds) {
        if (variant == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Product variant cannot be null");
        }

        log.debug("Updating attribute values for variant: {}", variant.getSku());
        
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            variant.setAttributeValues(new ArrayList<>());
            log.debug("Cleared all attribute values from variant: {}", variant.getSku());
        } else {
            List<AttributeValue> attributeValues = findByIds(attributeValueIds);
            variant.setAttributeValues(attributeValues);
            log.debug("Updated {} attribute values for variant: {}", attributeValues.size(), variant.getSku());
        }
    }

}
