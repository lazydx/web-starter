package com.ldx.webstarter.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Web Starter 루트 프로퍼티 클래스.
 * 
 * <p>web-starter의 모든 설정을 관리합니다.
 * 각 기능별 프로퍼티 클래스들을 중첩 프로퍼티로 포함합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "web-starter")
public class WebStarterProperties {
    
    /**
     * Enable web-starter features.
     */
    private boolean enabled = true;
    
    /**
     * Response configuration.
     */
    @NestedConfigurationProperty
    private ResponseProperties response = new ResponseProperties();
    
    /**
     * CORS configuration.
     */
    @NestedConfigurationProperty
    private CorsProperties cors = new CorsProperties();
    
    /**
     * Pagination configuration.
     */
    @NestedConfigurationProperty
    private PaginationProperties pagination = new PaginationProperties();
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public ResponseProperties getResponse() {
        return response;
    }
    
    public void setResponse(ResponseProperties response) {
        this.response = response;
    }
    
    public CorsProperties getCors() {
        return cors;
    }
    
    public void setCors(CorsProperties cors) {
        this.cors = cors;
    }
    
    public PaginationProperties getPagination() {
        return pagination;
    }
    
    public void setPagination(PaginationProperties pagination) {
        this.pagination = pagination;
    }
}