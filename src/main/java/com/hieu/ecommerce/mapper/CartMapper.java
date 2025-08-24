package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.response.CartResponseDTO;
import com.hieu.ecommerce.model.entity.Cart;
import com.hieu.ecommerce.model.entity.CartItem;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "totalAmount", expression = "java(mapTotalAmount(cart))")
    @Mapping(target = "totalItems", expression = "java(mapTotalItems(cart))")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "hasInvalidItems", expression = "java(hasInvalidItems(cart))")
    CartResponseDTO convertCartToCartResponseDTO(Cart cart);

    default BigDecimal mapTotalAmount(Cart cart) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalAmount;
    }

    default Integer mapTotalItems(Cart cart) {
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }

    default boolean hasInvalidItems(Cart cart) {
        return cart.getItems().stream().anyMatch(item ->
                !isItemAvailable(item.getProduct(), item.getProductVariant(), item.getQuantity()));
    }

    default boolean isItemAvailable(Product product, ProductVariant variant, int quantity) {
        return product.getStatus() == ProductStatus.ACTIVE
                && (variant != null ? variant.getStockQuantity() >= quantity : product.getStockQuantity() >= quantity);
    }
}

