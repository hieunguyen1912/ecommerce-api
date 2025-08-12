package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.ProductVariantResponse;
import com.hieu.ecommerce.model.entity.ProductVariant;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.hieu.ecommerce.model.entity.ProductVariantImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;


@Mapper(componentModel = "spring", uses = {AttributeValueMapper.class})
public interface ProductVariantMapper {

    @Mapping(target = "imageUrls", expression = "java(mapVariantImageUrls(productVariant))")
    @Mapping(target = "attributeValues", source = "attributeValues")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    default List<String> mapVariantImageUrls(ProductVariant variant) {
        if (variant != null && !CollectionUtils.isEmpty(variant.getImages())) {
            return variant.getImages().stream()
                    .map(ProductVariantImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return Collections.emptyList();
    }
}