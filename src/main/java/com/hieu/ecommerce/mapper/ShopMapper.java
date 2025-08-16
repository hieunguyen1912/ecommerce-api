package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CreateShopRequest;
import com.hieu.ecommerce.model.dto.response.ShopDetailResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopListResponseDTO;
import com.hieu.ecommerce.model.dto.response.ShopResponseDTO;
import com.hieu.ecommerce.model.entity.Shop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface ShopMapper {

    Shop toEntity(CreateShopRequest createShopRequest);

    @Mapping(target = "ownerName", expression = "java(shop.getUser().getFirstName() + \" \" + shop.getUser().getLastName())")
    ShopResponseDTO toShopResponseDTO(Shop shop);

    @Mapping(target = "ownerName", expression = "java(shop.getUser().getFirstName() + \" \" + shop.getUser().getLastName())")
    ShopListResponseDTO toShopListResponse(Shop shop);

    @Mapping(target = "products", source = "products")
    @Mapping(target = "ownerName", expression = "java(shop.getUser().getFirstName() + \" \" + shop.getUser().getLastName())")
    ShopDetailResponseDTO toshopDetailResponseDTO(Shop shop);
}
