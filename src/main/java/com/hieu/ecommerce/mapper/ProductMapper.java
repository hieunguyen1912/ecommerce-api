package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.ProductVariantImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class})
public interface ProductMapper {

    Product toProduct(CreateProductRequest request);

    @Mapping(target = "price", expression = "java(mapPrice(product))")
    @Mapping(target = "stockQuantity", expression = "java(mapStock(product))")
    @Mapping(target = "defaultImageUrl", expression = "java(mapDefaultImageUrl(product))")
    ProductSummaryResponse toProductSummaryResponse(Product product);

    @Mapping(target = "price", expression = "java(mapPrice(product))")
    @Mapping(target = "stockQuantity", expression = "java(mapStock(product))")
    @Mapping(target = "imageUrls", expression = "java(mapImageUrls(product))")
    @Mapping(target = "variants", source = "productVariant")
    ProductResponse toProductResponse(Product product);

    // Lấy giá sản phẩm - ưu tiên giá thấp nhất từ variants nếu có
    default BigDecimal mapPrice(Product product) {
        if (product.isHasVariants()
                && product.getProductVariant() != null
                && !product.getProductVariant().isEmpty()) {

            return product.getProductVariant()
                    .stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
        }
        return product.getPrice();
    }

    // Lấy tổng số lượng stock từ tất cả variants
    default Integer mapStock(Product product) {
        if (product.isHasVariants()
                && product.getProductVariant() != null
                && !product.getProductVariant().isEmpty()) {

            return product.getProductVariant()
                    .stream()
                    .map(ProductVariant::getStockQuantity)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum);
        }
        return product.getStockQuantity();
    }

    // Lấy ảnh của sản phẩm chính (chỉ khi không có variants)
    default List<String> mapImageUrls(Product product) {
        if (!product.isHasVariants() && !CollectionUtils.isEmpty(product.getImages())) {
            return product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return Collections.emptyList();
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