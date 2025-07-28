package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileValidationServiceTest {

    private FileValidationService fileValidationService;
    private FileStorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileStorageProperties();
        properties.getUpload().setMaxFileSize("1MB");
        properties.getUpload().setAllowedExtensions(List.of("jpg", "png", "pdf"));
        properties.getUpload().setAllowedMimeTypes(List.of("image/jpeg", "image/png", "application/pdf"));
        
        fileValidationService = new FileValidationService(properties);
    }

    @Test
    void validateFile_ValidFile_ShouldPass() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            "test content".getBytes()
        );

        assertDoesNotThrow(() -> fileValidationService.validateFile(file));
    }

    @Test
    void validateFile_EmptyFile_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        FileValidationService.FileValidationException exception = assertThrows(
            FileValidationService.FileValidationException.class,
            () -> fileValidationService.validateFile(file)
        );
        
        assertEquals("File cannot be empty", exception.getMessage());
    }

    @Test
    void validateFileName_ValidFileName_ShouldPass() {
        assertDoesNotThrow(() -> fileValidationService.validateFileName("test.jpg"));
    }

    @Test
    void validateFileName_FileNameWithPathTraversal_ShouldThrowException() {
        FileValidationService.FileValidationException exception = assertThrows(
            FileValidationService.FileValidationException.class,
            () -> fileValidationService.validateFileName("../test.jpg")
        );
        
        assertTrue(exception.getMessage().contains("invalid path sequence"));
    }

    @Test
    void validateFileSize_ValidSize_ShouldPass() {
        assertDoesNotThrow(() -> fileValidationService.validateFileSize(1000));
    }

    @Test
    void validateFileSize_TooLarge_ShouldThrowException() {
        FileValidationService.FileValidationException exception = assertThrows(
            FileValidationService.FileValidationException.class,
            () -> fileValidationService.validateFileSize(2 * 1024 * 1024) // 2MB
        );
        
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }

    @Test
    void validateFileExtension_AllowedExtension_ShouldPass() {
        assertDoesNotThrow(() -> fileValidationService.validateFileExtension("test.jpg"));
    }

    @Test
    void validateFileExtension_DisallowedExtension_ShouldThrowException() {
        FileValidationService.FileValidationException exception = assertThrows(
            FileValidationService.FileValidationException.class,
            () -> fileValidationService.validateFileExtension("test.exe")
        );
        
        assertTrue(exception.getMessage().contains("is not allowed"));
    }

    @Test
    void validateMimeType_AllowedMimeType_ShouldPass() {
        assertDoesNotThrow(() -> fileValidationService.validateMimeType("image/jpeg"));
    }

    @Test
    void validateMimeType_DisallowedMimeType_ShouldThrowException() {
        FileValidationService.FileValidationException exception = assertThrows(
            FileValidationService.FileValidationException.class,
            () -> fileValidationService.validateMimeType("application/x-executable")
        );
        
        assertTrue(exception.getMessage().contains("is not allowed"));
    }
}