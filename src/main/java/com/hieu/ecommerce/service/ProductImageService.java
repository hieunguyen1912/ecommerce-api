package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.entity.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    ProductImage uploadImage(Long productId, Long productVariantId, MultipartFile file, boolean isThumbnail);

    List<ProductImage> getImagesByProductId(Long productId);
   
    List<ProductImage> getImagesByVariantId(Long productVariantId);

    ProductImage getThumbnailByProductId(Long productId);

    ProductImage getImageById(Long imageId);

    void deleteImage(Long imageId);

    ProductImage setThumbnail(Long imageId);

    ProductImage moveImageToVariant(Long imageId, Long newProductId, Long newVariantId);

    List<ProductImage> uploadMultipleImages(Long productId, Long productVariantId, 
                                           List<MultipartFile> files, boolean setFirstAsThumbnail);
}

