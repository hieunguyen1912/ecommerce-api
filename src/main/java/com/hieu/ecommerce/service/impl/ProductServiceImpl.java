package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.common.SecurityUtil;
import com.hieu.ecommerce.common.constant.ProductStatus;
import com.hieu.ecommerce.common.constant.RoleName;
import com.hieu.ecommerce.common.constant.ShopStatus;
import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.ProductMapper;
import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.dto.response.ProductSummaryResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.CategoryRepository;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.repository.ShopRepository;
import com.hieu.ecommerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private static final String PRODUCT_NAME_EXISTS = "Product with name '%s' already exists";
    private static final String PRICE_REQUIRED = "Price is required for product without variants";
    private static final String STOCK_QUANTITY_REQUIRED = "Stock quantity is required for product without variants";
    private static final String IMAGES_REQUIRED = "At least one image is required for product without variants";

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ShopRepository shopRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductMapper productMapper,
                              ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.shopRepository = shopRepository;
    }

    @Override
    @Transactional
    public Product createBaseProduct(CreateProductRequest request, Shop shop, List<Category> categories) {
        logger.info("Creating product with request: {}", request);

        // Validate request
        validateCreateProductRequest(request);

        // Create and persist product first to obtain ID
        Product product = productMapper.toProduct(request);
        boolean hasVariants = hasVariants(request);
        product.setHasVariants(hasVariants);
        product.setShop(shop);
        product.setCategories(categories);

        return product;
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


    @Override
    public void updateProduct(Product product, UpdateProductRequest updateProductRequest) {
        validateUpdateRequest(updateProductRequest);
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

    private void validateUpdateRequest(UpdateProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }

        if (Boolean.TRUE.equals(request.getHasVariants())) {
            if (request.getVariants() == null || request.getVariants().isEmpty()) {
                throw new IllegalArgumentException("Product with variants must have at least one variant");
            }
            if (request.getPrice() != null || request.getStockQuantity() != null) {
                throw new IllegalArgumentException("Product with variants cannot have price or stock quantity at product level");
            }
        } else {
            if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Product without variants must have a valid price");
            }
            if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Product without variants must have a valid stock quantity");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getAllProductsByShopId(Pageable pageable) {
        logger.info("Fetching all products with pageable: {}", pageable);
        Shop shop = getCurrentUserShop();

        return productRepository.findAllByShopIdAndStatusNot(shop.getId(), ProductStatus.DELETED, pageable)
                .map(productMapper::toProductSummaryResponse).stream().toList();
    }

    @Override
    public List<ProductSummaryResponse> getAllProductsForUser(Pageable pageable) {
        return productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toProductSummaryResponse).stream().toList();
    }

    @Override
    public List<ProductSummaryResponse> getAllProductsForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toProductSummaryResponse).stream().toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return getProduct(id, RoleName.ADMIN);
    }

    @Override
    public ProductResponse getProductByIdAndShopId(Long id) {
        return getProduct(id, RoleName.SELLER);
    }

    @Override
    public ProductResponse getActiveProductById(Long id) {
        return getProduct(id, RoleName.USER);
    }

    private ProductResponse getProduct(Long id, RoleName roleName) {
        Product product = switch (roleName) {
            case SELLER -> {
                Shop shop = getCurrentUserShop();
                yield productRepository.findByIdAndShopId(id, shop.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product with id " + id + " not found in shop with id " + shop.getId()));
            }
            case USER -> productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active product with id " + id + " not found"));
            case ADMIN -> productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product with id " + id + " not found"));
        };

        return productMapper.toProductResponse(product);
    }

    @Override
    public void changeStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        product.setStatus(status);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProductForShop(Long id) {
        deleteProduct(id, RoleName.SELLER);
    }

    @Override
    public void deleteProductForAdmin(Long id) {
        deleteProduct(id, RoleName.ADMIN);
    }

    public void deleteProduct(Long id, RoleName roleName) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));

        if (roleName == RoleName.SELLER) {
            Shop shop = getCurrentUserShop();
            if (!product.getShop().getId().equals(shop.getId())) {
                throw new ResourceNotFoundException("Product not found in your shop");
            }
        }

        if (product.getStatus() == ProductStatus.DELETED) {
            logger.warn("Product with id {} is already deleted", id);
            return;
        }

        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        logger.info("Deleted product with id: {} by context: {}", id, roleName);
    }

    private Shop getCurrentUserShop() {
        Long userId = SecurityUtil.getCurrentUserId();
        return shopRepository.findByUserIdAndStatus(userId, ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for user ID: " + userId));
    }

    @Override
    public Integer countProducts() {
        return (int) productRepository.count();
    }
}