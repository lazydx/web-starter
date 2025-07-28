package com.ldx.webstarter.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    FileMetadata store(MultipartFile file);

    FileMetadata store(MultipartFile file, String directory);

    FileMetadata store(String filename, InputStream inputStream, long size, String contentType);

    FileMetadata store(String filename, InputStream inputStream, long size, String contentType, String directory);

    Resource loadAsResource(String filename);

    Resource loadAsResource(FileMetadata fileMetadata);

    InputStream loadAsInputStream(String filename);

    InputStream loadAsInputStream(FileMetadata fileMetadata);

    boolean exists(String filename);

    boolean exists(FileMetadata fileMetadata);

    void delete(String filename);

    void delete(FileMetadata fileMetadata);

    String getStorageType();

    FileMetadata getFileMetadata(String filename);
}