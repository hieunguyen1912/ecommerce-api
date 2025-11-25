package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.CartResponse;
import com.hieu.ecommerce.model.entity.Cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    CartResponse toCartResponse(Cart cart);
}

