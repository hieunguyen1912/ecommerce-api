package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.model.entity.Product;
import com.hieu.ecommerce.model.entity.ProductImage;
import com.hieu.ecommerce.model.entity.ProductVariant;
import com.hieu.ecommerce.repository.ProductImageRepository;
import com.hieu.ecommerce.repository.ProductRepository;
import com.hieu.ecommerce.repository.ProductVariantRepository;
import com.hieu.ecommerce.service.ProductImageService;
import com.hieu.ecommerce.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StorageService storageService;

    private static final String PRODUCT_IMAGES_FOLDER = "products";

    @Override
    @Transactional
    public ProductImage uploadImage(Long productId, Long productVariantId, MultipartFile file, boolean isThumbnail) {
        log.info("Uploading image for product {} and variant {}", productId, productVariantId);

        Product product = getProduct(productId);
        ProductVariant variant = getProductVariant(productVariantId, productId);

        String imageUrl = storageService.upload(file, PRODUCT_IMAGES_FOLDER);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setProductVariant(variant);
        productImage.setImageUrl(imageUrl);
        productImage.setThumbnail(isThumbnail);

        if (isThumbnail) {
            unsetOtherThumbnails(productId);
        }

        ProductImage savedImage = productImageRepository.save(productImage);
        log.info("Successfully uploaded image with id: {}", savedImage.getId());

        return savedImage;
    }

    @Override
    public List<ProductImage> getImagesByProductId(Long productId) {
        log.debug("Getting all images for product: {}", productId);
        return productImageRepository.findAllByProductId(productId);
    }

    @Override
    public List<ProductImage> getImagesByVariantId(Long productVariantId) {
        log.debug("Getting all images for variant: {}", productVariantId);
        return productImageRepository.findAllByProductVariantId(productVariantId);
    }

    @Override
    public ProductImage getThumbnailByProductId(Long productId) {
        log.debug("Getting thumbnail for product: {}", productId);
        return productImageRepository.findDefaultImageByProductId(productId);
    }

    @Override
    public ProductImage getImageById(Long imageId) {
        log.debug("Getting image with id: {}", imageId);
        return productImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "ProductImage with id " + imageId + " not found"));
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image with id: {}", imageId);

        ProductImage productImage = getImageById(imageId);
        String imageUrl = productImage.getImageUrl();

        productImageRepository.delete(productImage);

        try {
            storageService.delete(imageUrl);
            log.info("Successfully deleted image from GCS: {}", imageUrl);
        } catch (Exception e) {
            log.warn("Failed to delete image from GCS: {}", imageUrl, e);
        }

        log.info("Successfully deleted image with id: {}", imageId);
    }

    @Override
    @Transactional
    public ProductImage setThumbnail(Long imageId) {
        log.info("Setting image {} as thumbnail", imageId);

        ProductImage productImage = getImageById(imageId);
        Long productId = productImage.getProduct().getId();

        unsetOtherThumbnails(productId);

        productImage.setThumbnail(true);
        ProductImage updatedImage = productImageRepository.save(productImage);

        log.info("Successfully set image {} as thumbnail for product {}", imageId, productId);
        return updatedImage;
    }

    @Override
    @Transactional
    public ProductImage moveImageToVariant(Long imageId, Long newProductId, Long newVariantId) {
        log.info("Moving image {} to product {} and variant {}", imageId, newProductId, newVariantId);

        ProductImage productImage = getImageById(imageId);
        Long oldProductId = productImage.getProduct().getId();
        Long oldVariantId = productImage.getProductVariant().getId();

        Product newProduct = getProduct(newProductId);
        ProductVariant newVariant = getProductVariant(newVariantId, newProductId);

        if (oldProductId.equals(newProductId) && oldVariantId.equals(newVariantId)) {
            log.warn("Image {} is already assigned to product {} and variant {}", imageId, newProductId, newVariantId);
            return productImage;
        }

        productImage.setProduct(newProduct);
        productImage.setProductVariant(newVariant);


        boolean wasThumbnail = productImage.isThumbnail();
        if (wasThumbnail && !oldProductId.equals(newProductId)) {
            productImage.setThumbnail(false);
            log.info("Removed thumbnail status from image {} as it moved to different product", imageId);
        } else if (wasThumbnail && oldProductId.equals(newProductId)) {
            unsetOtherThumbnails(newProductId);
        }

        ProductImage updatedImage = productImageRepository.save(productImage);
        log.info("Successfully moved image {} from product {} variant {} to product {} variant {}", 
                imageId, oldProductId, oldVariantId, newProductId, newVariantId);

        return updatedImage;
    }

    @Override
    @Transactional
    public List<ProductImage> uploadMultipleImages(Long productId, Long productVariantId,
                                                    List<MultipartFile> files, boolean setFirstAsThumbnail) {
        log.info("Uploading {} images for product {} and variant {}", files.size(), productId, productVariantId);

        if (files.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Files list is empty");
        }

        getProduct(productId);
        getProductVariant(productVariantId, productId);

        List<ProductImage> images = files.stream()
                .map(file -> {
                    boolean isThumbnail = setFirstAsThumbnail && files.indexOf(file) == 0;
                    return uploadImage(productId, productVariantId, file, isThumbnail);
                })
                .toList();

        log.info("Successfully uploaded {} images", images.size());
        return images;
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Product with id " + productId + " not found"));
    }

    private ProductVariant getProductVariant(Long productVariantId, Long productId) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "ProductVariant with id " + productVariantId + " not found"));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new AppException(ErrorCode.INVALID_OPERATION,
                    "ProductVariant does not belong to the specified Product");
        }

        return variant;
    }

    private void unsetOtherThumbnails(Long productId) {
        List<ProductImage> thumbnailImages = productImageRepository.findAllByProductId(productId)
                .stream()
                .filter(ProductImage::isThumbnail)
                .toList();

        if (!thumbnailImages.isEmpty()) {
            thumbnailImages.forEach(img -> img.setThumbnail(false));
            productImageRepository.saveAll(thumbnailImages);
            log.debug("Unset {} thumbnails for product {}", thumbnailImages.size(), productId);
        }
    }
}

