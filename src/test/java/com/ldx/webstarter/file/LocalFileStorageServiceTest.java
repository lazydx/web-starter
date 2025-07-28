package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService fileStorageService;
    private FileStorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        properties.getLocal().setCreateDirectories(true);
        
        fileStorageService = new LocalFileStorageService(properties);
    }

    @Test
    void store_ValidFile_ShouldStoreSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );

        FileMetadata metadata = fileStorageService.store(file);

        assertNotNull(metadata);
        assertEquals("test.txt", metadata.getOriginalFileName());
        assertEquals("text/plain", metadata.getContentType());
        assertEquals(11, metadata.getSize());
        assertEquals("txt", metadata.getExtension());
        assertEquals("LOCAL", metadata.getStorageType());
        assertNotNull(metadata.getStoredFileName());
        assertNotNull(metadata.getChecksum());
    }

    @Test
    void store_WithDirectory_ShouldStoreInDirectory() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );

        FileMetadata metadata = fileStorageService.store(file, "documents");

        assertNotNull(metadata);
        assertTrue(metadata.getStoragePath().contains("documents"));
    }

    @Test
    void loadAsResource_ExistingFile_ShouldReturnResource() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );

        FileMetadata metadata = fileStorageService.store(file);
        Resource resource = fileStorageService.loadAsResource(metadata.getStoragePath());

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void exists_ExistingFile_ShouldReturnTrue() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );

        FileMetadata metadata = fileStorageService.store(file);
        boolean exists = fileStorageService.exists(metadata.getStoragePath());

        assertTrue(exists);
    }

    @Test
    void exists_NonExistingFile_ShouldReturnFalse() {
        boolean exists = fileStorageService.exists("nonexistent.txt");
        assertFalse(exists);
    }

    @Test
    void delete_ExistingFile_ShouldDeleteSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );

        FileMetadata metadata = fileStorageService.store(file);
        assertTrue(fileStorageService.exists(metadata.getStoragePath()));

        fileStorageService.delete(metadata.getStoragePath());
        assertFalse(fileStorageService.exists(metadata.getStoragePath()));
    }

    @Test
    void getStorageType_ShouldReturnLocal() {
        assertEquals("LOCAL", fileStorageService.getStorageType());
    }

    @Test
    void store_FileWithRelativePath_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "../test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> fileStorageService.store(file)
        );
        
        assertTrue(exception.getMessage().contains("relative path outside current directory"));
    }
}