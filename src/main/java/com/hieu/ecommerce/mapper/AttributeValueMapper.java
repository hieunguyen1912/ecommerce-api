package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.AttributeValueResponse;
import com.hieu.ecommerce.model.entity.AttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeValueMapper {

    @Mapping(target = "attributeName", source = "attribute.name")
    AttributeValueResponse toAttributeValueResponse(AttributeValue attributeValue);
}
