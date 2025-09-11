package com.ldx.webstarter.infrastructure.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * CORS 설정 프로퍼티 클래스.
 * 
 * <p>Cross-Origin Resource Sharing 설정을 관리합니다.
 * 허용 오리진, 메서드, 헤더 등을 설정할 수 있습니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "web-starter.cors")
public class CorsProperties {
    
    /**
     * Enable CORS configuration.
     */
    private boolean enabled = true;
    
    /**
     * Comma-separated list of origins to allow. Use "*" to allow all origins.
     */
    private List<String> allowedOrigins = List.of("*");
    
    /**
     * Comma-separated list of HTTP methods to allow.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    
    /**
     * Comma-separated list of headers to allow in a request.
     */
    private List<String> allowedHeaders = List.of("*");
    
    /**
     * Set whether credentials are supported.
     */
    private boolean allowCredentials = false;
    
    /**
     * How long the response to a pre-flight request can be cached by clients.
     */
    private Duration maxAge = Duration.ofMinutes(30);
    
    /**
     * Path pattern for CORS configuration.
     */
    private String pathPattern = "/**";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }
    
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }
    
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }
    
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }
    
    public boolean isAllowCredentials() {
        return allowCredentials;
    }
    
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
    
    public Duration getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(Duration maxAge) {
        this.maxAge = maxAge;
    }
    
    public String getPathPattern() {
        return pathPattern;
    }
    
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }
}