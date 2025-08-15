package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.response.ShopResponse;
import com.hieu.ecommerce.model.entity.Shop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShopMapper {

    Shop toEntity(CreateShopRequest createShopRequest);

    @Mapping(target = "ownerName", expression = "java(shop.getUser().getFirstName() + \" \" + shop.getUser().getLastName())")
    ShopResponse toResponsee(Shop shop);
}
