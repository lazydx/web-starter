package com.ldx.webstarter.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "web-starter.file-storage")
public class FileStorageProperties {
    
    private boolean enabled = true;
    
    private Upload upload = new Upload();
    
    private Download download = new Download();
    
    private Local local = new Local();
    
    private Azure azure = new Azure();
    
    private Format format = new Format();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Azure getAzure() {
        return azure;
    }

    public void setAzure(Azure azure) {
        this.azure = azure;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public static class Upload {
        private String maxFileSize = "10MB";
        private String maxRequestSize = "100MB";
        private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt");
        private List<String> allowedMimeTypes = List.of(
            "image/jpeg", "image/png", "image/gif", 
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        );
        private boolean enableVirusScanning = false;
        private String tempDir = "${java.io.tmpdir}/webstarter-uploads";
        private int fileContentValidationHeaderSize = 512;

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public String getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }

        public List<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        public void setAllowedExtensions(List<String> allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
        }

        public List<String> getAllowedMimeTypes() {
            return allowedMimeTypes;
        }

        public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
            this.allowedMimeTypes = allowedMimeTypes;
        }

        public boolean isEnableVirusScanning() {
            return enableVirusScanning;
        }

        public void setEnableVirusScanning(boolean enableVirusScanning) {
            this.enableVirusScanning = enableVirusScanning;
        }

        public String getTempDir() {
            return tempDir;
        }

        public void setTempDir(String tempDir) {
            this.tempDir = tempDir;
        }

        public int getFileContentValidationHeaderSize() {
            return fileContentValidationHeaderSize;
        }

        public void setFileContentValidationHeaderSize(int fileContentValidationHeaderSize) {
            this.fileContentValidationHeaderSize = fileContentValidationHeaderSize;
        }
    }

    public static class Download {
        private boolean enableRangeRequests = true;
        private long cacheMaxAge = 3600;
        private String defaultContentType = "application/octet-stream";

        public boolean isEnableRangeRequests() {
            return enableRangeRequests;
        }

        public void setEnableRangeRequests(boolean enableRangeRequests) {
            this.enableRangeRequests = enableRangeRequests;
        }

        public long getCacheMaxAge() {
            return cacheMaxAge;
        }

        public void setCacheMaxAge(long cacheMaxAge) {
            this.cacheMaxAge = cacheMaxAge;
        }

        public String getDefaultContentType() {
            return defaultContentType;
        }

        public void setDefaultContentType(String defaultContentType) {
            this.defaultContentType = defaultContentType;
        }
    }

    public static class Local {
        private String basePath = "./uploads";
        private boolean createDirectories = true;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public boolean isCreateDirectories() {
            return createDirectories;
        }

        public void setCreateDirectories(boolean createDirectories) {
            this.createDirectories = createDirectories;
        }
    }

    public static class Azure {
        private boolean enabled = false;
        private String connectionString;
        private String containerName;
        private String pathPrefix = "uploads/";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getPathPrefix() {
            return pathPrefix;
        }

        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }
    }

    public static class Format {
        /**
         * 파일 크기 포맷팅을 위한 바이트 변환 단위.
         * 기본값: 1024 (이진 단위), 1000으로 설정하면 SI 단위 사용
         */
        private int bytesPerUnit = 1024;

        public int getBytesPerUnit() {
            return bytesPerUnit;
        }

        public void setBytesPerUnit(int bytesPerUnit) {
            this.bytesPerUnit = bytesPerUnit;
        }
    }
}