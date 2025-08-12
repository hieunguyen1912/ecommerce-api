package com.hieu.ecommerce.model.dto.response;

import lombok.Data;

@Data
public class AttributeValueResponse {
    private Long id;
    private String attributeName;
    private String value;
}