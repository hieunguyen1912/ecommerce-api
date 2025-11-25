package com.hieu.ecommerce.service.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final Storage storage;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Value("${gcs.base-url:https://storage.googleapis.com}")
    private String baseUrl;

    private static final String DEFAULT_FOLDER = "uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public String upload(MultipartFile file) {
        return upload(file, DEFAULT_FOLDER);
    }

    @Override
    public String upload(MultipartFile file, String folderPath) {
        log.info("Uploading file: {} to folder: {}", file.getOriginalFilename(), folderPath);

        validateFile(file);

        try {
            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFileName = generateUniqueFileName(fileExtension);
            String blobPath = folderPath != null && !folderPath.isEmpty() 
                    ? folderPath + "/" + uniqueFileName 
                    : uniqueFileName;

            // Tạo BlobInfo với content type
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, blobPath))
                    .setContentType(file.getContentType())
                    .build();

            // Upload file lên GCS
            storage.create(blobInfo, file.getBytes());

            // Tạo public URL
            String publicUrl = baseUrl + "/" + bucketName + "/" + blobPath;

            log.info("File uploaded successfully. URL: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Error reading file: {}", file.getOriginalFilename(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, 
                    "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading file to GCS: {}", file.getOriginalFilename(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, 
                    "Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        log.info("Deleting file: {}", fileUrl);

        try {
            // Trích xuất blob path từ URL
            String blobPath = extractBlobPathFromUrl(fileUrl);

            if (blobPath == null || blobPath.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, 
                        "Invalid file URL format");
            }

            // Xóa file từ GCS
            BlobId blobId = BlobId.of(bucketName, blobPath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("File deleted successfully: {}", blobPath);
            } else {
                log.warn("File not found or already deleted: {}", blobPath);
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                        "File not found: " + fileUrl);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting file from GCS: {}", fileUrl, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, 
                    "Failed to delete file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, 
                    "File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.INVALID_REQUEST, 
                    "File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_REQUEST, 
                    "File must be an image");
        }
    }

    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private String extractBlobPathFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        // Xử lý các format URL khác nhau
        // Format: https://storage.googleapis.com/bucket-name/path/to/file.jpg
        // Hoặc: gs://bucket-name/path/to/file.jpg
        // Hoặc: path/to/file.jpg (path trực tiếp)

        if (fileUrl.startsWith("gs://")) {
            // Format: gs://bucket-name/path/to/file.jpg
            String withoutPrefix = fileUrl.substring(5); // Remove "gs://"
            int firstSlash = withoutPrefix.indexOf('/');
            if (firstSlash != -1) {
                return withoutPrefix.substring(firstSlash + 1);
            }
            return null;
        } else if (fileUrl.startsWith("https://") || fileUrl.startsWith("http://")) {
            // Format: https://storage.googleapis.com/bucket-name/path/to/file.jpg
            // Hoặc: https://storage.cloud.google.com/bucket-name/path/to/file.jpg
            String withoutProtocol = fileUrl.replaceFirst("^https?://", "");
            String[] parts = withoutProtocol.split("/", 3);
            if (parts.length >= 3) {
                // parts[0] là domain, parts[1] là bucket-name, parts[2] là path
                return parts[2];
            } else if (parts.length == 2) {
                // Có thể chỉ có domain và bucket
                return "";
            }
            return null;
        } else {
            // Assume it's a direct path
            return fileUrl;
        }
    }
}

