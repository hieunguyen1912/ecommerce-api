package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.response.OrderItemResponse;
import com.hieu.ecommerce.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", source = "productVariant.id")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "productImage", source = "productImage")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "subTotal", source = "subTotal")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

}
