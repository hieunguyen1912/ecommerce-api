package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.ProductStatus;
import com.hieu.ecommerce.constant.VariantStatus;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.ProductMapper;
import com.hieu.ecommerce.model.dto.request.*;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.CategoryRepository;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.service.ProductService;
import com.hieu.ecommerce.service.ProductVariantService;
import com.hieu.ecommerce.specification.SearchOperation;
import com.hieu.ecommerce.specification.SpecificationsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductVariantService productVariantService;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE);
        }

        Product product = productMapper.toProduct(request);
        product.setCategories(categories);
        product.setStatus(ProductStatus.ACTIVE);

        log.debug("Created product entity with id: {}", product.getId());

        productVariantService.createProductVariants(request.getVariants(), product);

        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }


    @Override
    @Transactional
    public ProductResponse updateProductBasicInfo(Long productId, UpdateProductRequest request) {
        log.info("Updating basic info for product with id: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Product not found"));
        
        if (!product.getName().equals(request.getName()) &&
            !productRepository.existsByName(request.getName())) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }

        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            product.setCategories(categories);
            log.debug("Updated categories for product: {}", product.getName());
        }
        
        product = productRepository.save(product);
        log.info("Successfully updated basic info for product with id: {}", productId);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse addVariant(Long productId, CreateProductVariantRequest request) {
        log.info("Adding variant to product with id: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product with id " + productId + " not found"));
        productVariantService.addVariant(product, request);
        
        productRepository.save(product);
        
        log.info("Successfully added variant to product with id: {}", productId);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse removeVariant(Long productId, Long variantId) {
        log.info("Removing variant {} from product with id: {}", variantId, productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product with id " + productId + " not found"));
        productVariantService.removeVariant(product, variantId);

        productRepository.save(product);
        
        log.info("Successfully removed variant {} from product with id: {}", variantId, productId);
        return productMapper.toProductResponse(product);
    }

    @Override
    public Page<ProductSummaryResponse> getAllProducts(Pageable pageable, ProductFilterRequest filter) {

        SpecificationsBuilder<Product> builder = new SpecificationsBuilder<>();

        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            if (filter.getStatuses().size() == 1) {
                builder.with("status", SearchOperation.EQUALITY, filter.getStatuses().get(0));
            } else {
                builder.with("status", SearchOperation.IN, filter.getStatuses());
            }
        }

        if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
            builder.with("categories.id", SearchOperation.IN, filter.getCategoryIds());
        }

        if (filter.getMinPrice() != null) {
            builder.with("productVariant.price", SearchOperation.GREATER_THAN_OR_EQUAL, filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            builder.with("productVariant.price", SearchOperation.LESS_THAN_OR_EQUAL, filter.getMaxPrice());
        }

        Specification<Product> filterSpec = builder.build();

        Page<Product> products = filterSpec != null
            ? productRepository.findAll(filterSpec, pageable)
            : productRepository.findAll(pageable);

        return products.map(productMapper::toProductSummaryResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product with id " + id + " not found"));
        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse getActiveProductById(Long id) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Active product with id " + id + " not found"));
        return productMapper.toProductResponse(product);
    }

    @Override
    public Product getActiveProduct(Long id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product with id " + id + " not found"));
    }

    @Override
    public void changeStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product with id " + id + " not found"));
        product.setStatus(status);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Product with id " + id + " not found"));

        if (product.getStatus() == ProductStatus.DELETED) {
            log.warn("Product with id {} is already deleted", id);
            return;
        }

        if (product.getProductVariant() != null && !product.getProductVariant().isEmpty()) {
            product.getProductVariant().forEach(
                    productVariant -> productVariant.setStatus(VariantStatus.DELETED)
            );
        }

        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        log.info("Deleted product with id: {}", id);
    }

    @Override
    public Integer countProducts() {
        return (int) productRepository.count();
    }

    private String generateDefaultSku(Product product) {
        String productName = product.getName()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        String sku = productName + "-DEFAULT";
        
        if (sku.length() > 50) {
            sku = sku.substring(0, 47) + "-DF";
        }
        
        return sku;
    }
}