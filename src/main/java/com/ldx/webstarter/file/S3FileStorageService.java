package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class S3FileStorageService implements FileStorageService {

    private final FileStorageProperties properties;

    public S3FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        validateS3Configuration();
    }

    private void validateS3Configuration() {
        FileStorageProperties.S3 s3Config = properties.getS3();
        if (!StringUtils.hasText(s3Config.getBucketName())) {
            throw new IllegalArgumentException("S3 bucket name must be configured");
        }
        if (!StringUtils.hasText(s3Config.getAccessKey()) || !StringUtils.hasText(s3Config.getSecretKey())) {
            throw new IllegalArgumentException("S3 access credentials must be configured");
        }
    }

    @Override
    public FileMetadata store(MultipartFile file) {
        return store(file, null);
    }

    @Override
    public FileMetadata store(MultipartFile file, String directory) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        
        if (filename.contains("..")) {
            throw new RuntimeException("Cannot store file with relative path outside current directory: " + filename);
        }

        try {
            return store(filename, file.getInputStream(), file.getSize(), file.getContentType(), directory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file to S3: " + filename, e);
        }
    }

    @Override
    public FileMetadata store(String filename, InputStream inputStream, long size, String contentType) {
        return store(filename, inputStream, size, contentType, null);
    }

    @Override
    public FileMetadata store(String filename, InputStream inputStream, long size, String contentType, String directory) {
        String originalFilename = filename;
        String extension = getFileExtension(filename);
        String storedFilename = generateUniqueFilename(filename);
        String s3Key = buildS3Key(storedFilename, directory);
        
        String checksum = calculateChecksum(inputStream);
        
        FileMetadata metadata = new FileMetadata(
            UUID.randomUUID().toString(),
            originalFilename,
            storedFilename,
            contentType,
            size,
            extension,
            s3Key,
            getStorageType()
        );
        metadata.setChecksum(checksum);
        
        return metadata;
    }

    @Override
    public Resource loadAsResource(String filename) {
        return new InputStreamResource(loadAsInputStream(filename));
    }

    @Override
    public Resource loadAsResource(FileMetadata fileMetadata) {
        return loadAsResource(fileMetadata.getStoragePath());
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        throw new UnsupportedOperationException("S3 service implementation requires AWS SDK dependencies. " +
            "Please add AWS SDK dependencies and implement S3 client integration.");
    }

    @Override
    public InputStream loadAsInputStream(FileMetadata fileMetadata) {
        return loadAsInputStream(fileMetadata.getStoragePath());
    }

    @Override
    public boolean exists(String filename) {
        throw new UnsupportedOperationException("S3 service implementation requires AWS SDK dependencies. " +
            "Please add AWS SDK dependencies and implement S3 client integration.");
    }

    @Override
    public boolean exists(FileMetadata fileMetadata) {
        return exists(fileMetadata.getStoragePath());
    }

    @Override
    public void delete(String filename) {
        throw new UnsupportedOperationException("S3 service implementation requires AWS SDK dependencies. " +
            "Please add AWS SDK dependencies and implement S3 client integration.");
    }

    @Override
    public void delete(FileMetadata fileMetadata) {
        delete(fileMetadata.getStoragePath());
    }

    @Override
    public String getStorageType() {
        return "S3";
    }

    @Override
    public FileMetadata getFileMetadata(String filename) {
        throw new UnsupportedOperationException("S3 service implementation requires AWS SDK dependencies. " +
            "Please add AWS SDK dependencies and implement S3 client integration.");
    }

    private String buildS3Key(String filename, String directory) {
        StringBuilder keyBuilder = new StringBuilder();
        
        String pathPrefix = properties.getS3().getPathPrefix();
        if (StringUtils.hasText(pathPrefix)) {
            keyBuilder.append(pathPrefix);
            if (!pathPrefix.endsWith("/")) {
                keyBuilder.append("/");
            }
        }
        
        if (StringUtils.hasText(directory)) {
            keyBuilder.append(directory);
            if (!directory.endsWith("/")) {
                keyBuilder.append("/");
            }
        }
        
        keyBuilder.append(filename);
        
        return keyBuilder.toString();
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        if (StringUtils.hasText(extension)) {
            return timestamp + "_" + uuid + "." + extension;
        } else {
            return timestamp + "_" + uuid;
        }
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private String calculateChecksum(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            return null;
        }
    }
}