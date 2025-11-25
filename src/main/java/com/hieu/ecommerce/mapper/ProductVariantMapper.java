package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.ProductVariantResponse;
import com.hieu.ecommerce.model.entity.ProductVariant;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {AttributeValueMapper.class})
public interface ProductVariantMapper {

    @Mapping(target = "attributeValues", source = "attributeValues")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);
}