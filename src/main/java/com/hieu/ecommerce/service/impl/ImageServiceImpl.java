package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.model.dto.request.UpdateProductRequest;
import com.hieu.ecommerce.model.dto.request.UpdateProductVariantRequest;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.model.entity.ProductVariantImage;
import com.hieu.ecommerce.repository.ProductImageRepository;
import com.hieu.ecommerce.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final ProductImageRepository productImageRepository;

    public ImageServiceImpl(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }
    @Override
    public List<ProductImage> createProductImages(List<String> imageUrls, Product product) {
        logger.debug("Creating {} images for product: {}", imageUrls.size(), product.getName());

        if (imageUrls.isEmpty()) {
            logger.warn("No image URLs provided for product: {}", product.getName());
            return Collections.emptyList();
        }

        List<ProductImage> images = IntStream.range(0, imageUrls.size())
                .mapToObj(i -> buildProductImage(imageUrls.get(i), product, i == 0))
                .filter(Objects::nonNull)
                .toList();

        if (images.isEmpty()) {
            logger.warn("No valid images found for product: {}", product.getName());
            return Collections.emptyList();
        }

        product.setImages(images);

        return images;
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
    public void updateProductImages(Product product, UpdateProductRequest updateProductRequest) {
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

        if (!CollectionUtils.isEmpty(updateProductRequest.getNewImages())) {
            List<ProductImage> newImages = updateProductRequest.getNewImages().stream()
                    .map(imageRequest -> buildProductImage(imageRequest.getImageUrl(), product, imageRequest.isDefault())
                    ).toList();

            product.getImages().addAll(newImages);
            logger.debug("Added {} new images to product: {}", newImages.size(), product.getName());
        }
    }


    @Override
    public void updateVariantImages(ProductVariant productVariant, UpdateProductVariantRequest variantReq) {
        // Handle existing images to keep
        if (!CollectionUtils.isEmpty(variantReq.getKeepImageIds())) {
            int initialImageCount = productVariant.getImages().size();
            productVariant.getImages().removeIf(
                    image -> !variantReq.getKeepImageIds().contains(image.getId())
            );
            int removedCount = initialImageCount - productVariant.getImages().size();
            if (removedCount > 0) {
                logger.debug("Removed {} images from variant: {}", removedCount, productVariant.getSku());
            }
        } else {
            // Clear all existing images if no keepImageIds specified
            if (!CollectionUtils.isEmpty(productVariant.getImages())) {
                logger.debug("Clearing all images for variant: {}", productVariant.getSku());
                productVariant.getImages().clear();
            }
        }

        // Add new images
        if (!CollectionUtils.isEmpty(variantReq.getNewImages())) {
            List<ProductVariantImage> newImages = variantReq.getNewImages()
                    .stream().map(
                            request -> buildProductVariantImage(request.getImageUrl(), productVariant, request.isDefault())
                    ).toList();
            productVariant.getImages().addAll(newImages);
            logger.debug("Added {} new images to variant: {}", newImages.size(), productVariant.getSku());
        }
    }

    @Override
    public ProductVariantImage buildProductVariantImage(String imageUrl, ProductVariant productVariant, boolean isDefault) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        ProductVariantImage productVariantImage = new ProductVariantImage();
        productVariantImage.setProductVariant(productVariant);
        productVariantImage.setImageUrl(imageUrl.trim());
        productVariantImage.setDefault(isDefault);
        return productVariantImage;
    }
}
