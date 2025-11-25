package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.VariantStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.ProductVariantMapper;
import com.hieu.ecommerce.model.dto.request.CreateProductVariantRequest;
import com.hieu.ecommerce.model.dto.request.UpdateVariantRequest;
import com.hieu.ecommerce.model.dto.response.ProductVariantResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.ProductVariantRepository;
import com.hieu.ecommerce.service.AttributeValueService;
import com.hieu.ecommerce.service.ProductVariantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueService attributeValueService;
    private final ProductVariantMapper productVariantMapper;
   

    @Override
    public void createProductVariants(List<CreateProductVariantRequest> variantRequests, Product product) {
        if (CollectionUtils.isEmpty(variantRequests)) {
            log.warn("No variant requests provided for product: {}", product.getName());
            return;
        }

        log.info("Creating {} variants for product: {}", variantRequests.size(), product.getName());

        List<ProductVariant> variants = variantRequests.stream()
                .map(request -> createSingleVariant(request, product))
                .toList();

        product.getProductVariant().clear();
        product.getProductVariant().addAll(variants);

        log.info("Successfully created {} variants for product: {}", variants.size(), product.getName());
    }

    @Override
    public ProductVariant createSingleVariant(CreateProductVariantRequest request, Product product) {
        log.debug("Creating variant with SKU: {} for product: {}", request.getSku(), product.getName());

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(normalizeSku(request.getSku()))
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .status(VariantStatus.ACTIVE)
                .build();

        List<AttributeValue> attributeValues = attributeValueService.findByIds(request.getAttributeValueIds());
        variant.setAttributeValues(attributeValues);

        log.debug("Successfully created variant with SKU: {} for product: {}", variant.getSku(), product.getName());
        return variant;
    }

    private String normalizeSku(String sku) {
        if (sku == null) {
            return null;
        }
        return sku.trim().toUpperCase();
    }

    @Override
    public void addVariant(Product product, CreateProductVariantRequest request) {
        log.info("Adding variant to product with id: {}", product.getId());
        
        String normalizedSku = normalizeSku(request.getSku());
        if (productVariantRepository.existsByProductAndSku(product, normalizedSku)) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE,
                String.format("Duplicate product variant with SKU %s already exists for this product", normalizedSku));
        }
        
        ProductVariant variant = createSingleVariant(request, product);
        productVariantRepository.save(variant);
        product.getProductVariant().add(variant);
        
        log.info("Successfully added variant with SKU: {} to product with id: {}", variant.getSku(), product.getId());
    }

    @Override
    public void removeVariant(Product product, Long variantId) {
        log.info("Removing variant {} from product {}", variantId, product.getId());
        
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Variant with id " + variantId + " not found"));
        
        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new AppException(ErrorCode.INVALID_OPERATION,
                "Variant does not belong to this product");
        }
        
        if (product.getProductVariant().size() <= 1) {
            throw new AppException(ErrorCode.INVALID_OPERATION,
                "Product must have at least one variant. Cannot remove the last variant");
        }
        
        product.getProductVariant().remove(variant);
        
        log.info("Successfully removed variant {} from product {}", variantId, product.getId());
    }

    @Override
    public ProductVariantResponse updateProductVariant(Long productId, Long variantId, UpdateVariantRequest request) {
        log.info("Updating variant {} of product {}", variantId, productId);
        
        ProductVariant variant =  productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Variant with id " + variantId + " not found"));
        
        if (!variant.getProduct().getId().equals(productId)) {
            throw new AppException(ErrorCode.INVALID_OPERATION,
                "Variant does not belong to this product");
        }
        
        if (request.getSku() != null) {
            String normalizedSku = normalizeSku(request.getSku());
            
            if (!normalizedSku.equals(variant.getSku()) &&
                    productVariantRepository.existsByProductAndSku(variant.getProduct(), normalizedSku)) {
                    throw new AppException(ErrorCode.DUPLICATE_RESOURCE,
                        String.format("SKU %s already exists for this product", normalizedSku));
                }

            variant.setSku(normalizedSku);
        }
        
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }
        
        if (request.getStock() != null) {
            variant.setStock(request.getStock());
        }
        
        if (request.getAttributeValueIds() != null) {
            attributeValueService.updateAttributeValues(variant, request.getAttributeValueIds());
        }
        
        productVariantRepository.save(variant);
        log.info("Successfully updated variant {} of product {}", variantId, productId);
        
        return productVariantMapper.toProductVariantResponse(variant);
    }
}
