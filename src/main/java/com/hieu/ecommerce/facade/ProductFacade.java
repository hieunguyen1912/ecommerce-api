package com.hieu.ecommerce.facade;

import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.ProductMapper;
import com.hieu.ecommerce.model.dto.request.CreateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.response.ProductResponse;
import com.hieu.ecommerce.model.entity.*;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductFacade {
    private static final Logger logger = LoggerFactory.getLogger(ProductFacade.class);

    private final ProductService productService;
    private final ImageService imageService;
    private final CategoryService categoryService;
    private final ShopService shopService;
    private final ProductVariantService productVariantService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;


    public ProductFacade(ProductService productService, ImageService imageService, CategoryService categoryService, ShopService shopService, ProductVariantService productVariantService, ProductMapper productMapper, ProductRepository productRepository) {
        this.productService = productService;
        this.imageService = imageService;
        this.categoryService = categoryService;
        this.shopService = shopService;
        this.productVariantService = productVariantService;
        this.productMapper = productMapper;
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        List<Category> categories = categoryService.getCategoriesByIds(request.getCategoryIds());
        Shop shop = shopService.getCurrentUserShop();
        Product product = productService.createBaseProduct(request, shop, categories);

        if (product.isHasVariants()) {
            logger.debug("Handling product with variants");
            productVariantService.createProductVariants(request.getVariants(), product);
        } else {
            logger.debug("Handling simple product");

            List<ProductImage> images = imageService.createProductImages(request.getImageUrls(), product);

            logger.info("Created {} images for product: {}", images.size(), product.getName());
        }
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        logger.info("Updating product with id: {} and request: {}", productId, request);

        List<Category> categories = categoryService.getCategoriesByIds(request.getCategoryIds());
        Shop shop = shopService.getCurrentUserShop();
        Product product = productRepository.findByIdAndShopId(productId, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or doesn't belong to your shop"));
        product.setCategories(categories);

        productService.updateProduct(product, request);
        if (!product.isHasVariants()) {
            logger.info("Handling simple product");
            imageService.updateProductImages(product, request);
        } else {
            logger.info("Handling product with variants");
            productVariantService.updateProductVariants(product, request);
        }
        return productMapper.toProductResponse(product);
    }

}
