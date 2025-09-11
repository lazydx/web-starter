package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 파일 업로드 컨트롤러.
 * 
 * <p>Spring MVC 요구사항으로 인해 @RestController 어노테이션 유지.
 * Component Scan 독립성을 위해 AutoConfiguration에서 Package Scan 설정.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final FileValidationService fileValidationService;

    public FileUploadController(FileStorageService fileStorageService, 
                               FileValidationService fileValidationService) {
        this.fileStorageService = fileStorageService;
        this.fileValidationService = fileValidationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(value = "directory", required = false) String directory) {
        try {
            fileValidationService.validateFile(file);
            FileMetadata metadata = fileStorageService.store(file, directory);
            return ResponseEntity.ok(metadata);
        } catch (FileValidationService.FileValidationException e) {
            throw new BusinessException("FILE_VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("FILE_UPLOAD_ERROR", "Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileMetadata>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "directory", required = false) String directory) {
        
        List<FileMetadata> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                fileValidationService.validateFile(file);
                FileMetadata metadata = fileStorageService.store(file, directory);
                uploadedFiles.add(metadata);
            } catch (FileValidationService.FileValidationException e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": Failed to upload - " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && uploadedFiles.isEmpty()) {
            throw new BusinessException("FILE_UPLOAD_ERROR", 
                "All files failed to upload: " + String.join("; ", errors));
        }

        return ResponseEntity.ok(uploadedFiles);
    }

    @GetMapping("/{filename}/metadata")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable String filename) {
        try {
            FileMetadata metadata = fileStorageService.getFileMetadata(filename);
            if (metadata == null) {
                throw new BusinessException("FILE_NOT_FOUND", "File not found: " + filename);
            }
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            throw new BusinessException("FILE_METADATA_ERROR", "Failed to get file metadata: " + e.getMessage());
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteFile(@PathVariable String filename) {
        try {
            if (!fileStorageService.exists(filename)) {
                throw new BusinessException("FILE_NOT_FOUND", "File not found: " + filename);
            }
            
            fileStorageService.delete(filename);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("FILE_DELETE_ERROR", "Failed to delete file: " + e.getMessage());
        }
    }

    @GetMapping("/{filename}/exists")
    public ResponseEntity<Boolean> fileExists(@PathVariable String filename) {
        try {
            boolean exists = fileStorageService.exists(filename);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            throw new BusinessException("FILE_CHECK_ERROR", "Failed to check file existence: " + e.getMessage());
        }
    }
}