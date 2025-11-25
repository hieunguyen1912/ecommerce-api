package com.hieu.ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file, String folderPath);

    String upload(MultipartFile file);

    void delete(String fileUrl);
}
