package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileValidationService {

    private final FileStorageProperties properties;

    public FileValidationService(FileStorageProperties properties) {
        this.properties = properties;
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File cannot be empty");
        }

        validateFileName(file.getOriginalFilename());
        validateFileSize(file.getSize());
        validateFileExtension(file.getOriginalFilename());
        validateMimeType(file.getContentType());
        
        if (properties.getUpload().isEnableVirusScanning()) {
            validateFileContent(file);
        }
    }

    public void validateFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new FileValidationException("Filename cannot be empty");
        }

        if (filename.contains("..")) {
            throw new FileValidationException("Filename contains invalid path sequence: " + filename);
        }

        if (filename.contains("/") || filename.contains("\\")) {
            throw new FileValidationException("Filename cannot contain path separators: " + filename);
        }

        String[] invalidChars = {"<", ">", ":", "\"", "|", "?", "*"};
        for (String invalidChar : invalidChars) {
            if (filename.contains(invalidChar)) {
                throw new FileValidationException("Filename contains invalid character '" + invalidChar + "': " + filename);
            }
        }
    }

    public void validateFileSize(long fileSize) {
        DataSize maxSize = DataSize.parse(properties.getUpload().getMaxFileSize());
        
        if (fileSize > maxSize.toBytes()) {
            throw new FileValidationException(
                String.format("File size (%d bytes) exceeds maximum allowed size (%s)", 
                    fileSize, properties.getUpload().getMaxFileSize())
            );
        }
    }

    public void validateFileExtension(String filename) {
        String extension = getFileExtension(filename);
        
        if (!StringUtils.hasText(extension)) {
            throw new FileValidationException("File must have an extension: " + filename);
        }

        List<String> allowedExtensions = properties.getUpload().getAllowedExtensions();
        
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension.toLowerCase())) {
            throw new FileValidationException(
                String.format("File extension '%s' is not allowed. Allowed extensions: %s", 
                    extension, allowedExtensions)
            );
        }
    }

    public void validateMimeType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw new FileValidationException("File content type cannot be determined");
        }

        List<String> allowedMimeTypes = properties.getUpload().getAllowedMimeTypes();
        
        if (!allowedMimeTypes.isEmpty() && !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new FileValidationException(
                String.format("File content type '%s' is not allowed. Allowed types: %s", 
                    contentType, allowedMimeTypes)
            );
        }
    }

    public void validateFileContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            validateFileContent(inputStream);
        } catch (IOException e) {
            throw new FileValidationException("Could not read file content for validation", e);
        }
    }

    public void validateFileContent(InputStream inputStream) {
        try {
            byte[] header = new byte[512];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead > 0) {
                if (containsSuspiciousPattern(header)) {
                    throw new FileValidationException("File contains suspicious content and may be malicious");
                }
            }
        } catch (IOException e) {
            throw new FileValidationException("Could not validate file content", e);
        }
    }

    private boolean containsSuspiciousPattern(byte[] content) {
        String contentStr = new String(content).toLowerCase();
        
        String[] suspiciousPatterns = {
            "<?php", "<script", "javascript:", "vbscript:", 
            "onload=", "onerror=", "eval(", "exec(",
            "shell_exec", "system(", "passthru(",
            "base64_decode", "gzinflate"
        };
        
        for (String pattern : suspiciousPatterns) {
            if (contentStr.contains(pattern)) {
                return true;
            }
        }
        
        return false;
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

    public static class FileValidationException extends RuntimeException {
        public FileValidationException(String message) {
            super(message);
        }

        public FileValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}