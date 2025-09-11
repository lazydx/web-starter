package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.file.FileNameGenerator;
import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;
    private final FileStorageProperties properties;
    private final FileNameGenerator fileNameGenerator;

    public LocalFileStorageService(FileStorageProperties properties, FileNameGenerator fileNameGenerator) {
        this.properties = properties;
        this.fileNameGenerator = fileNameGenerator;
        this.rootLocation = Paths.get(properties.getLocal().getBasePath()).normalize().toAbsolutePath();
        
        if (properties.getLocal().isCreateDirectories()) {
            try {
                Files.createDirectories(this.rootLocation);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory: " + this.rootLocation, e);
            }
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
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public FileMetadata store(String filename, InputStream inputStream, long size, String contentType) {
        return store(filename, inputStream, size, contentType, null);
    }

    @Override
    public FileMetadata store(String filename, InputStream inputStream, long size, String contentType, String directory) {
        try {
            String originalFilename = filename;
            String extension = fileNameGenerator.extractFileExtension(filename);
            String storedFilename = fileNameGenerator.generateUniqueFilename(filename, directory);
            
            Path targetLocation = resolveTargetLocation(storedFilename, directory);
            
            if (targetLocation.getParent() != null) {
                Files.createDirectories(targetLocation.getParent());
            }
            
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String checksum = calculateChecksum(targetLocation);
            String relativePath = rootLocation.relativize(targetLocation).toString();
            
            FileMetadata metadata = new FileMetadata(
                UUID.randomUUID().toString(),
                originalFilename,
                storedFilename,
                contentType,
                size,
                extension,
                relativePath,
                getStorageType()
            );
            metadata.setChecksum(checksum);
            
            return metadata;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            
            // 경로가 rootLocation 내부에 있는지 보안 검증
            if (!file.startsWith(rootLocation)) {
                throw new RuntimeException("Path traversal attempt detected: " + filename);
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + filename + " at path: " + file.toString());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    @Override
    public Resource loadAsResource(FileMetadata fileMetadata) {
        return loadAsResource(fileMetadata.getStoragePath());
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    @Override
    public InputStream loadAsInputStream(FileMetadata fileMetadata) {
        return loadAsInputStream(fileMetadata.getStoragePath());
    }

    @Override
    public boolean exists(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        return Files.exists(file);
    }

    @Override
    public boolean exists(FileMetadata fileMetadata) {
        return exists(fileMetadata.getStoragePath());
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + filename, e);
        }
    }

    @Override
    public void delete(FileMetadata fileMetadata) {
        delete(fileMetadata.getStoragePath());
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    @Override
    public FileMetadata getFileMetadata(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            
            if (!Files.exists(file)) {
                return null;
            }
            
            long size = Files.size(file);
            String contentType = Files.probeContentType(file);
            String extension = fileNameGenerator.extractFileExtension(filename);
            String checksum = calculateChecksum(file);
            
            FileMetadata metadata = new FileMetadata(
                UUID.randomUUID().toString(),
                file.getFileName().toString(),
                file.getFileName().toString(),
                contentType,
                size,
                extension,
                rootLocation.relativize(file).toString(),
                getStorageType()
            );
            metadata.setChecksum(checksum);
            
            return metadata;
            
        } catch (IOException e) {
            throw new RuntimeException("Could not get file metadata: " + filename, e);
        }
    }

    private Path resolveTargetLocation(String filename, String directory) {
        if (StringUtils.hasText(directory)) {
            return rootLocation.resolve(directory).resolve(filename).normalize();
        } else {
            return rootLocation.resolve(filename).normalize();
        }
    }


    private String calculateChecksum(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file);
            byte[] digest = md.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}