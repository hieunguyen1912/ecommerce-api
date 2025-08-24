package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.model.dto.request.CreateProductVariantRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductVariantRequest;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.repository.ProductVariantRepository;
import com.hieu.ecommerce.service.AttributeValueService;
import com.hieu.ecommerce.service.ImageService;

import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductVariantServiceImpl implements com.hieu.ecommerce.service.ProductVariantService {
    private static final Logger logger = LoggerFactory.getLogger(ProductVariantServiceImpl.class);

    // Constants for validation messages
    private static final String REQUEST_NULL = "Request cannot be null";
    private static final String SKU_BLANK = "SKU cannot be null or empty";
    private static final String PRICE_NEGATIVE = "Price must be non-negative";
    private static final String STOCK_NEGATIVE = "Stock quantity must be non-negative";
    private static final String ATTRIBUTE_VALUES_EMPTY = "Attribute values cannot be empty";
    private static final String DUPLICATE_SKU = "Duplicate product variant with SKU %s already exists for this product";

    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueService attributeValueService;
    private final ProductRepository productRepository;
    private final ImageService imageService;

    public ProductVariantServiceImpl(ProductVariantRepository productVariantRepository,
                                     AttributeValueService attributeValueService,
                                     ProductRepository productRepository, ImageService imageService) {
        this.productVariantRepository = productVariantRepository;
        this.attributeValueService = attributeValueService;
        this.productRepository = productRepository;
        this.imageService = imageService;
    }

    @Override
    public void createProductVariants(List<CreateProductVariantRequest> variantRequests, Product product) {
        logger.info("Creating {} variants for product: {}", variantRequests.size(), product.getName());

        List<ProductVariant> variants = new ArrayList<>();
        variantRequests.forEach(
                variantRequest -> variants.add(
                        createSingleVariant(variantRequest, product)
                )
        );

        product.getProductVariant().clear();
        product.getProductVariant().addAll(variants);
    }

    @Override
    public ProductVariant createSingleVariant(CreateProductVariantRequest request, Product product) {
        validateCreateProductVariantRequest(request);

        if (productVariantRepository.existsByProductAndSku(product, request.getSku())) {
            throw new IllegalArgumentException(String.format(DUPLICATE_SKU, request.getSku()));
        }

        ProductVariant productVariant = buildProductVariant(request, product);
        product.getProductVariant().add(productVariant);

        logger.debug("Successfully created variant with SKU: {} for product: {}", 
                    productVariant.getSku(), product.getName());

        return productVariant;
    }

    private void validateCreateProductVariantRequest(CreateProductVariantRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_NULL);
        }
        if (StringUtils.isBlank(request.getSku())) {
            throw new IllegalArgumentException(SKU_BLANK);
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(PRICE_NEGATIVE);
        }
        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            throw new IllegalArgumentException(STOCK_NEGATIVE);
        }
        if (CollectionUtils.isEmpty(request.getAttributeValues())) {
            throw new IllegalArgumentException(ATTRIBUTE_VALUES_EMPTY);
        }
    }

    private ProductVariant buildProductVariant(CreateProductVariantRequest request, Product product) {
        ProductVariant productVariant = new ProductVariant();
        productVariant.setProduct(product);
        productVariant.setSku(request.getSku());
        productVariant.setPrice(request.getPrice());
        productVariant.setStockQuantity(request.getStockQuantity());

        // Set attribute values
        if (!CollectionUtils.isEmpty(request.getAttributeValues())) {
            List<AttributeValue> attributeValues = request.getAttributeValues().stream()
                    .map(attributeValueService::createAttributeValue)
                    .toList();
            productVariant.setAttributeValues(attributeValues);
        }

        // Set images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductVariantImage> images = request.getImageUrls()
                    .stream().map(req -> imageService.buildProductVariantImage(req, productVariant, true))
                    .toList();
            productVariant.setImages(images);
        }

        return productVariant;
    }

    @Override
    public void updateProductVariants(Product product, UpdateProductRequest updateProductRequest) {
        if (CollectionUtils.isEmpty(updateProductRequest.getVariants())) {
            if (!CollectionUtils.isEmpty(product.getProductVariant())) {
                logger.info("Clearing all variants for product: {}", product.getName());
                product.getProductVariant().clear();
                productRepository.flush();
            }
            return;
        }

        // Collect existing variant IDs to keep
        Set<Long> keepVariantIds = updateProductRequest.getVariants().stream()
                .map(UpdateProductVariantRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove variants that are not in the keep list
        int initialSize = product.getProductVariant().size();
        product.getProductVariant().removeIf(variant ->
                !keepVariantIds.contains(variant.getId()));
        int removedCount = initialSize - product.getProductVariant().size();
        if (removedCount > 0) {
            productRepository.flush();
            logger.info("Removed {} variants from product: {}", removedCount, product.getName());
        }

        // Index remaining variants by id for O(1) lookup
        Map<Long, ProductVariant> idToVariant = product.getProductVariant().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        // Process each variant request
        for (UpdateProductVariantRequest variantReq : updateProductRequest.getVariants()) {
            if (variantReq.getId() != null) {
                ProductVariant existingVariant = idToVariant.get(variantReq.getId());
                if (existingVariant == null) {
                    throw new ResourceNotFoundException("Variant with id " + variantReq.getId() + " not found");
                }
                updateExistingVariant(existingVariant, variantReq, product.getName());
            } else {
                createNewVariant(product, variantReq);
            }
        }
    }

    @Override
    public ProductVariant getProductVariant(Long productVariantId) {
        if (productVariantId == null) return null;
        return productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant with id " + productVariantId + " not found"));
    }

    private void createNewVariant(Product product, UpdateProductVariantRequest variantReq) {
        logger.info("Creating new variant for product: {}, SKU: {}", product.getName(), variantReq.getSku());
        
        // Validate variant request
        validateVariantRequest(variantReq);
        
        ProductVariant variant = new ProductVariant();
        variant.setSku(variantReq.getSku().trim());
        variant.setPrice(variantReq.getPrice());
        variant.setStockQuantity(variantReq.getStockQuantity());
        variant.setProduct(product);

        // Add images if provided
        if (!CollectionUtils.isEmpty(variantReq.getNewImages())) {
            List<ProductVariantImage> newImages = variantReq.getNewImages()
                    .stream().map(
                            request -> imageService.buildProductVariantImage(request.getImageUrl(), variant, request.isDefault())
                    ).toList();
            variant.setImages(newImages);
            logger.debug("Added {} images to new variant: {}", newImages.size(), variant.getSku());
        }

        // Add attribute values
        List<AttributeValue> attributeValues = attributeValueService.findOrCreateAll(variantReq.getAttributeValues());
        variant.setAttributeValues(attributeValues);
        logger.debug("Added {} attribute values to new variant: {}", attributeValues.size(), variant.getSku());

        product.getProductVariant().add(variant);
        logger.info("Successfully created new variant with SKU: {} for product: {}", variant.getSku(), product.getName());
    }

    private void updateExistingVariant(ProductVariant productVariant, UpdateProductVariantRequest variantReq, String productName) {
        logger.info("Updating existing variant with id: {} for product: {}", variantReq.getId(), productName);

        // Validate variant request
       validateVariantRequest(variantReq);

        // Update basic variant information
        productVariant.setSku(variantReq.getSku().trim());
        productVariant.setPrice(variantReq.getPrice());
        productVariant.setStockQuantity(variantReq.getStockQuantity());

        // Update variant images
        imageService.updateVariantImages(productVariant, variantReq);

        // Update variant attribute values
        attributeValueService.replaceVariantAttributeValues(productVariant, variantReq.getAttributeValues());

        logger.info("Successfully updated variant with id: {} for product: {}", variantReq.getId(), productName);
    }

    public void validateVariantRequest(UpdateProductVariantRequest variantReq) {
        if (variantReq.getSku() == null || variantReq.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (variantReq.getPrice() == null || variantReq.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Variant price must be greater than 0");
        }
        if (variantReq.getStockQuantity() == null || variantReq.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Variant stock quantity cannot be negative");
        }
        if (variantReq.getAttributeValues() == null || variantReq.getAttributeValues().isEmpty()) {
            throw new IllegalArgumentException("Variant must have at least one attribute value");
        }
    }
}
