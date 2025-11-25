package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.Product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class, CategoryMapper.class})
public interface ProductMapper {

    Product toProduct(CreateProductRequest request);

    ProductSummaryResponse toProductSummaryResponse(Product product);

    @Mapping(target = "variants", source = "productVariant")
    @Mapping(target = "categories", source = "categories")
    ProductResponse toProductResponse(Product product);

}