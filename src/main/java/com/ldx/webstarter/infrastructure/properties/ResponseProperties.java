package com.ldx.webstarter.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 응답 표준화 관련 프로퍼티 클래스.
 * 
 * <p>API 응답 형식 통일화를 위한 설정을 관리합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "web-starter.response")
public class ResponseProperties {
    
    /**
     * Enable response standardization.
     */
    private boolean enabled = true;
    
    /**
     * Enable automatic response wrapping for controller methods.
     */
    private boolean wrapResponse = true;
    
    /**
     * Include request ID in response.
     */
    private boolean includeRequestId = true;
    
    /**
     * Include timestamp in response.
     */
    private boolean includeTimestamp = true;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isWrapResponse() {
        return wrapResponse;
    }
    
    public void setWrapResponse(boolean wrapResponse) {
        this.wrapResponse = wrapResponse;
    }
    
    public boolean isIncludeRequestId() {
        return includeRequestId;
    }
    
    public void setIncludeRequestId(boolean includeRequestId) {
        this.includeRequestId = includeRequestId;
    }
    
    public boolean isIncludeTimestamp() {
        return includeTimestamp;
    }
    
    public void setIncludeTimestamp(boolean includeTimestamp) {
        this.includeTimestamp = includeTimestamp;
    }
}