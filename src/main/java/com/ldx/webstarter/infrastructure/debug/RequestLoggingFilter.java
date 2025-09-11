package com.ldx.webstarter.infrastructure.debug;

import com.ldx.webstarter.infrastructure.properties.DebugProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 요청/응답 로깅 필터.
 * 
 * <p>디버그 모드가 활성화된 경우 모든 HTTP 요청과 응답을 상세히 로깅합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    private final DebugProperties debugProperties;

    public RequestLoggingFilter(DebugProperties debugProperties) {
        this.debugProperties = debugProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        if (!debugProperties.isEnabled() || !debugProperties.isLogRequests()) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // 요청 로깅
            logRequest(wrappedRequest, startTime);
            
            // 실제 요청 처리
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // 응답 로깅
            logResponse(wrappedRequest, wrappedResponse, startTime);
            
        } finally {
            // 응답 데이터를 실제 클라이언트에게 전송
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, long startTime) {
        try {
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("timestamp", System.currentTimeMillis());
            requestInfo.put("method", request.getMethod());
            requestInfo.put("uri", request.getRequestURI());
            requestInfo.put("queryString", request.getQueryString());
            requestInfo.put("remoteAddr", request.getRemoteAddr());
            requestInfo.put("userAgent", request.getHeader("User-Agent"));
            
            // 헤더 정보
            Map<String, String> headers = new HashMap<>();
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                if (!isSensitiveHeader(headerName)) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            });
            requestInfo.put("headers", headers);
            
            logger.info("=== HTTP REQUEST ===");
            logger.info("Request Info: {}", requestInfo);
            
            // 요청 본문 로깅
            if (debugProperties.isLogRequestBody() && hasRequestBody(request)) {
                String requestBody = getRequestBody(request);
                if (requestBody != null && !requestBody.trim().isEmpty()) {
                    logger.info("Request Body: {}", truncateIfNeeded(requestBody, debugProperties.getMaxRequestBodyLogSize()));
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to log request", e);
        }
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long startTime) {
        try {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> responseInfo = new HashMap<>();
            responseInfo.put("status", response.getStatus());
            responseInfo.put("contentType", response.getContentType());
            responseInfo.put("contentLength", response.getContentSize());
            responseInfo.put("duration", duration + "ms");
            
            // 응답 헤더 정보
            Map<String, String> headers = new HashMap<>();
            response.getHeaderNames().forEach(headerName -> {
                if (!isSensitiveHeader(headerName)) {
                    headers.put(headerName, response.getHeader(headerName));
                }
            });
            responseInfo.put("headers", headers);
            
            logger.info("=== HTTP RESPONSE ===");
            logger.info("Response Info: {}", responseInfo);
            
            // 성능 메트릭 로깅
            if (debugProperties.isLogPerformanceMetrics() && debugProperties.getPerformance().isEnabled()) {
                logPerformanceMetrics(request, response, duration);
            }
            
            // 응답 본문 로깅
            if (debugProperties.isLogResponseBody() && hasResponseBody(response)) {
                String responseBody = getResponseBody(response);
                if (responseBody != null && !responseBody.trim().isEmpty()) {
                    logger.info("Response Body: {}", truncateIfNeeded(responseBody, debugProperties.getMaxResponseBodyLogSize()));
                }
            }
            
            logger.info("=====================");
            
        } catch (Exception e) {
            logger.warn("Failed to log response", e);
        }
    }

    private void logPerformanceMetrics(ContentCachingRequestWrapper request, 
                                     ContentCachingResponseWrapper response, long duration) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("endpoint", request.getMethod() + " " + request.getRequestURI());
        metrics.put("duration", duration);
        metrics.put("status", response.getStatus());
        metrics.put("requestSize", request.getContentLength());
        metrics.put("responseSize", response.getContentSize());
        
        // 성능 경고 (설정 기반)
        if (!debugProperties.getPerformance().isEnabled() || !debugProperties.getPerformance().isCollectMetrics()) {
            return; // 성능 모니터링이 비활성화된 경우
        }
        
        long slowThreshold = debugProperties.getPerformance().getSlowRequestThreshold();
        long alertThreshold = debugProperties.getPerformance().getPerformanceAlertThreshold();
        
        if (duration > slowThreshold) {
            logger.warn("SLOW REQUEST DETECTED: {}", metrics);
        } else if (duration > alertThreshold) {
            logger.warn("Performance Alert: {}", metrics);
        } else {
            logger.debug("Performance Metrics: {}", metrics);
        }
        
        // 추가 성능 분석
        logLargeRequestResponse(request, response, metrics);
    }

    private boolean hasRequestBody(ContentCachingRequestWrapper request) {
        return request.getContentLength() > 0 || 
               "POST".equalsIgnoreCase(request.getMethod()) || 
               "PUT".equalsIgnoreCase(request.getMethod()) || 
               "PATCH".equalsIgnoreCase(request.getMethod());
    }

    private boolean hasResponseBody(ContentCachingResponseWrapper response) {
        return response.getContentSize() > 0;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String truncateIfNeeded(String content, int maxSize) {
        if (content.length() <= maxSize) {
            return content;
        }
        return content.substring(0, maxSize) + "... [TRUNCATED - total length: " + content.length() + "]";
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCase = headerName.toLowerCase();
        return lowerCase.contains("authorization") || 
               lowerCase.contains("cookie") || 
               lowerCase.contains("token") || 
               lowerCase.contains("password");
    }

    /**
     * 큰 요청/응답 크기에 대한 별도 로깅.
     */
    private void logLargeRequestResponse(ContentCachingRequestWrapper request, 
                                       ContentCachingResponseWrapper response, 
                                       Map<String, Object> metrics) {
        long requestSize = request.getContentLength();
        long responseSize = response.getContentSize();
        
        long requestThreshold = debugProperties.getPerformance().getLargeRequestSizeThreshold();
        long responseThreshold = debugProperties.getPerformance().getLargeResponseSizeThreshold();
        
        if (requestSize > requestThreshold) {
            logger.warn("LARGE REQUEST DETECTED - Size: {} bytes, Endpoint: {}", 
                       requestSize, metrics.get("endpoint"));
        }
        
        if (responseSize > responseThreshold) {
            logger.warn("LARGE RESPONSE DETECTED - Size: {} bytes, Endpoint: {}", 
                       responseSize, metrics.get("endpoint"));
        }
    }
}