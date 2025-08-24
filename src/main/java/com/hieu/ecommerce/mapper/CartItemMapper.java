package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.model.dto.response.CartItemResponseDTO;
import com.hieu.ecommerce.model.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImage", expression = "java(mapDefaultImageUrl(cartItem.getProduct()))")
    @Mapping(target = "productVariantId", source = "productVariant.id")
    @Mapping(target = "variantName", source = "productVariant.sku")
    @Mapping(target = "unitPrice", expression = "java(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getPrice() : cartItem.getProduct().getPrice())")
    @Mapping(target = "priceSnapshot", source = "price")
    @Mapping(target = "totalPrice", expression = "java(cartItem.getPrice().multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))")
    @Mapping(target = "sellerName", expression = "java(cartItem.getProduct().getShop() != null ? cartItem.getProduct().getShop().getShopName() : \"Unknown\")")
    @Mapping(target = "available", expression = "java(isItemAvailable(cartItem.getProduct(), cartItem.getProductVariant(), cartItem.getQuantity()))")
    CartItemResponseDTO toDto(CartItem cartItem);

    default boolean isItemAvailable(Product product, ProductVariant variant, int quantity) {
        return product.getStatus() == ProductStatus.ACTIVE
                && (variant != null ? variant.getStockQuantity() >= quantity : product.getStockQuantity() >= quantity);
    }

    default String mapDefaultImageUrl(Product product) {
        if (!product.isHasVariants() && !CollectionUtils.isEmpty(product.getImages())) {
            return product.getImages().stream()
                    .filter(ProductImage::isDefault)
                    .map(ProductImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(product.getImages().stream()
                            .map(ProductImage::getImageUrl)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null));
        }
        if (!CollectionUtils.isEmpty(product.getProductVariant())) {
            return product.getProductVariant().stream()
                    .flatMap(variant -> variant.getImages().stream())
                    .filter(ProductVariantImage::isDefault)
                    .map(ProductVariantImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(product.getProductVariant().stream()
                            .flatMap(variant -> variant.getImages().stream())
                            .map(ProductVariantImage::getImageUrl)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null));
        }
        return null;
    }
}
