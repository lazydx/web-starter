package com.ldx.webstarter.infrastructure.autoconfigure;

import com.ldx.webstarter.file.*;
import com.ldx.webstarter.infrastructure.file.DefaultFileNameGenerator;
import com.ldx.webstarter.infrastructure.file.FileNameGenerator;
import com.ldx.webstarter.infrastructure.file.FileNamingProperties;
import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({FileStorageProperties.class, FileNamingProperties.class})
@ConditionalOnProperty(prefix = "web-starter.file-storage", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileNameGenerator fileNameGenerator(FileNamingProperties properties) {
        return new DefaultFileNameGenerator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileValidationService fileValidationService(FileStorageProperties properties) {
        return new FileValidationService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileStorageService localFileStorageService(FileStorageProperties properties, FileNameGenerator fileNameGenerator) {
        return new LocalFileStorageService(properties, fileNameGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileUploadController fileUploadController(FileStorageService fileStorageService,
                                                   FileValidationService fileValidationService) {
        return new FileUploadController(fileStorageService, fileValidationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileDownloadController fileDownloadController(FileStorageService fileStorageService,
                                                       FileStorageProperties properties) {
        return new FileDownloadController(fileStorageService, properties);
    }
}