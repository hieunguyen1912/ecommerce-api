package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.ProductMapper;
import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.request.CreateProductVariantRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.service.ProductService;
import com.hieu.ecommerce.service.ProductValidationService;
import com.hieu.ecommerce.service.ProductVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.IntStream;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    private static final String PRODUCT_NAME_EXISTS = "Product with name '%s' already exists";
    private static final String PRICE_REQUIRED = "Price is required for product without variants";
    private static final String STOCK_QUANTITY_REQUIRED = "Stock quantity is required for product without variants";
    private static final String IMAGES_REQUIRED = "At least one image is required for product without variants";
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductVariantService productVariantService;
    private final ProductValidationService productValidationService;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductMapper productMapper,
                              ProductVariantService productVariantService, 
                              ProductValidationService productValidationService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productVariantService = productVariantService;
        this.productValidationService = productValidationService;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        logger.info("Creating product with request: {}", request);
        
        // Validate request
        validateCreateProductRequest(request);
        
        // Create and persist product first to obtain ID
        Product product = productMapper.toProduct(request);
        boolean hasVariants = hasVariants(request);
        product.setHasVariants(hasVariants);
        
        Product savedProduct = productRepository.save(product);
        
        if (hasVariants) {
            createProductVariants(request.getVariants(), savedProduct);
        } else {
            createProductImages(request.getImageUrls(), savedProduct);
        }
        
        return productMapper.toProductResponse(savedProduct);
    }

    private void validateCreateProductRequest(CreateProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(String.format(PRODUCT_NAME_EXISTS, request.getName()));
        }

        boolean hasVariants = hasVariants(request);
        
        if (!hasVariants) {
            validateSimpleProductRequest(request);
        }
    }

    private boolean hasVariants(CreateProductRequest request) {
        return request.getVariants() != null && !request.getVariants().isEmpty();
    }

    private void validateSimpleProductRequest(CreateProductRequest request) {
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException(PRICE_REQUIRED);
        }
        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            throw new IllegalArgumentException(STOCK_QUANTITY_REQUIRED);
        }
        if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new IllegalArgumentException(IMAGES_REQUIRED);
        }
    }

    private void createProductVariants(List<CreateProductVariantRequest> variants, Product product) {
        if (variants != null) {
            variants.forEach(variantReq -> 
                productVariantService.createProductVariant(variantReq, product)
            );
            logger.debug("Created {} variants for product: {}", variants.size(), product.getName());
        }
    }

    private void createProductImages(List<String> imageUrls, Product product) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<ProductImage> images = IntStream.range(0, imageUrls.size())
                .mapToObj(i -> buildProductImage(imageUrls.get(i), product, i == 0))
                .filter(Objects::nonNull)
                .toList();
            
            if (images.isEmpty()) {
                logger.warn("No valid images found for product: {}", product.getName());
                return;
            }
            
            product.setImages(images);
            logger.debug("Created {} images for product: {}", images.size(), product.getName());
        }
    }

    private ProductImage buildProductImage(String imageUrl, Product product, boolean isDefault) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            logger.warn("Skipping null or empty image URL for product: {}", product.getName());
            return null;
        }
        
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl.trim());
        image.setDefault(isDefault);
        return image;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        logger.info("Updating product with id: {} and request: {}", id, request);
        
        // Validate request
        productValidationService.validateUpdateRequest(request);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        
        logger.info("Found product: {}, variants: {}, images: {}", 
                   product.getName(), product.getProductVariant().size(), product.getImages().size());

        updateBasicInfo(product, request);
        updateProductImages(product, request);
        productVariantService.updateProductVariants(product, request);

        // No explicit save necessary; dirty checking will persist changes on commit
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private void updateBasicInfo(Product product, UpdateProductRequest updateProductRequest) {
        // Update basic information
        if (updateProductRequest.getName() != null && !updateProductRequest.getName().trim().isEmpty()) {
            product.setName(updateProductRequest.getName().trim());
        }
        
        if (updateProductRequest.getDescription() != null) {
            product.setDescription(updateProductRequest.getDescription().trim());
        }

        // Handle price and stock quantity based on variant configuration
        if (Boolean.TRUE.equals(updateProductRequest.getHasVariants())) {
            product.setHasVariants(true);
            product.setPrice(null);
            product.setStockQuantity(0);
            logger.debug("Product {} configured as variant-based product", product.getName());
        } else {
            product.setHasVariants(false);
            if (updateProductRequest.getPrice() != null) {
                product.setPrice(updateProductRequest.getPrice());
            }
            if (updateProductRequest.getStockQuantity() != null) {
                product.setStockQuantity(updateProductRequest.getStockQuantity());
            }
            logger.debug("Product {} configured as simple product with price: {} and stock: {}", 
                       product.getName(), product.getPrice(), product.getStockQuantity());
        }
    }

    private void updateProductImages(Product product, UpdateProductRequest updateProductRequest) {
        // Handle existing images to keep
        if (!CollectionUtils.isEmpty(updateProductRequest.getKeepImageIds())) {
            int initialImageCount = product.getImages().size();
            product.getImages().removeIf(
                    image -> !updateProductRequest.getKeepImageIds().contains(image.getId())
            );
            int removedCount = initialImageCount - product.getImages().size();
            if (removedCount > 0) {
                logger.debug("Removed {} images from product: {}", removedCount, product.getName());
            }
        } else {
            // Clear all existing images if no keepImageIds specified
            if (!CollectionUtils.isEmpty(product.getImages())) {
                logger.debug("Clearing all images for product: {}", product.getName());
                product.getImages().clear();
            }
        }

        // Add new images
        if (!CollectionUtils.isEmpty(updateProductRequest.getNewImages())) {
            List<ProductImage> newImages = updateProductRequest.getNewImages().stream()
                    .map(imageRequest -> {
                        ProductImage image = new ProductImage();
                        image.setDefault(imageRequest.isDefault());
                        image.setImageUrl(imageRequest.getImageUrl());
                        image.setProduct(product);
                        return image;
                    }).toList();
            
            product.getImages().addAll(newImages);
            logger.debug("Added {} new images to product: {}", newImages.size(), product.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getAllProducts(Pageable pageable) {
        logger.info("Fetching all products with pageable: {}", pageable);
        return productRepository.findAll(pageable)
                .map(product -> {
                    ProductSummaryResponse response = productMapper.toProductSummaryResponse(product);
                    logger.info("Mapped ProductSummary: id={}, name={}, price={}, stockQuantity={}, defaultImageUrl={}",
                            response.getId(),
                            response.getName(),
                            response.getPrice(),
                            response.getStockQuantity(),
                            response.getDefaultImageUrl());
                    return response;
                }).stream().toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + id + " not found"));
        return productMapper.toProductResponse(product);
    }

    @Override
    public Integer countProducts() {
        return (int) productRepository.count();
    }
}

