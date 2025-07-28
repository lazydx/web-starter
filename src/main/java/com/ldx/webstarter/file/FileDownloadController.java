package com.ldx.webstarter.file;

import com.ldx.webstarter.infrastructure.exception.BusinessException;
import com.ldx.webstarter.infrastructure.properties.FileStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private final FileStorageService fileStorageService;
    private final FileStorageProperties properties;

    public FileDownloadController(FileStorageService fileStorageService, 
                                 FileStorageProperties properties) {
        this.fileStorageService = fileStorageService;
        this.properties = properties;
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename,
                                               @RequestParam(value = "inline", defaultValue = "false") boolean inline,
                                               HttpServletRequest request) {
        try {
            if (!fileStorageService.exists(filename)) {
                throw new BusinessException("FILE_NOT_FOUND", "File not found: " + filename);
            }

            Resource resource = fileStorageService.loadAsResource(filename);
            FileMetadata metadata = fileStorageService.getFileMetadata(filename);

            String contentType = determineContentType(metadata, request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            
            String encodedFilename = URLEncoder.encode(metadata.getOriginalFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
            
            if (inline) {
                headers.setContentDispositionFormData("inline", encodedFilename);
            } else {
                headers.setContentDispositionFormData("attachment", encodedFilename);
            }
            
            if (properties.getDownload().getCacheMaxAge() > 0) {
                headers.setCacheControl("max-age=" + properties.getDownload().getCacheMaxAge());
            }

            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("FILE_DOWNLOAD_ERROR", "Failed to download file: " + e.getMessage());
        }
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename,
                                           HttpServletRequest request) {
        return downloadFile(filename, true, request);
    }

    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamFile(@PathVariable String filename,
                                             HttpServletRequest request,
                                             @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            if (!fileStorageService.exists(filename)) {
                throw new BusinessException("FILE_NOT_FOUND", "File not found: " + filename);
            }

            Resource resource = fileStorageService.loadAsResource(filename);
            FileMetadata metadata = fileStorageService.getFileMetadata(filename);
            
            String contentType = determineContentType(metadata, request);
            long contentLength = resource.contentLength();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(contentLength);
            
            if (properties.getDownload().isEnableRangeRequests() && rangeHeader != null) {
                return handleRangeRequest(resource, metadata, rangeHeader, contentLength, contentType);
            }

            headers.add("Accept-Ranges", "bytes");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("FILE_STREAM_ERROR", "Failed to stream file: " + e.getMessage());
        }
    }

    private ResponseEntity<Resource> handleRangeRequest(Resource resource, FileMetadata metadata, 
                                                       String rangeHeader, long contentLength, 
                                                       String contentType) throws IOException {
        
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty() ? 
            Long.parseLong(ranges[1]) : contentLength - 1;

        if (start >= contentLength || end >= contentLength || start > end) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes */" + contentLength);
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .headers(headers)
                .build();
        }

        long rangeLength = end - start + 1;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(rangeLength);
        headers.add("Content-Range", String.format("bytes %d-%d/%d", start, end, contentLength));
        headers.add("Accept-Ranges", "bytes");

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .headers(headers)
            .body(resource);
    }

    private String determineContentType(FileMetadata metadata, HttpServletRequest request) {
        String contentType = metadata.getContentType();
        
        if (contentType == null || contentType.equals("application/octet-stream")) {
            try {
                contentType = request.getServletContext()
                    .getMimeType(metadata.getOriginalFileName());
            } catch (Exception e) {
            }
        }
        
        if (contentType == null) {
            contentType = properties.getDownload().getDefaultContentType();
        }
        
        return contentType;
    }
}