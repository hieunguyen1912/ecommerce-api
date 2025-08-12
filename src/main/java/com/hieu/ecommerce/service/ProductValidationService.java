package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductVariantRequest;

public interface ProductValidationService {
    
    /**
     * Validate product update request
     * @param request Update request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateUpdateRequest(UpdateProductRequest request);
    
    /**
     * Validate variant update request
     * @param variantReq Variant request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateVariantRequest(UpdateProductVariantRequest variantReq);
}
