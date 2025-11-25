package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.CartItemResponse;
import com.hieu.ecommerce.model.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemResponse toCartItemResponse(CartItem cartItem);
}
