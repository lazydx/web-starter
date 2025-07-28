package com.ldx.webstarter.infrastructure.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 페이지네이션 제한 프로퍼티 클래스.
 * 
 * <p>페이지네이션 요청에 대한 자동 제한을 설정합니다.
 * 기본 페이지 크기, 최대 페이지 크기, 최대 요소 수를 관리합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "web-starter.pagination")
public class PaginationProperties {
    
    /**
     * Enable pagination limit.
     */
    private boolean enabled = true;
    
    /**
     * Default page size.
     */
    @Min(1)
    @Max(100)
    private int defaultSize = 20;
    
    /**
     * Maximum page size allowed.
     */
    @Min(1)
    @Max(1000)
    private int maxSize = 100;
    
    /**
     * Maximum total elements allowed.
     */
    @Min(1)
    private long maxElements = 5000L;
    
    /**
     * Name of the page parameter.
     */
    private String pageParameter = "page";
    
    /**
     * Name of the size parameter.
     */
    private String sizeParameter = "size";
    
    /**
     * Name of the sort parameter.
     */
    private String sortParameter = "sort";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getDefaultSize() {
        return defaultSize;
    }
    
    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public long getMaxElements() {
        return maxElements;
    }
    
    public void setMaxElements(long maxElements) {
        this.maxElements = maxElements;
    }
    
    public String getPageParameter() {
        return pageParameter;
    }
    
    public void setPageParameter(String pageParameter) {
        this.pageParameter = pageParameter;
    }
    
    public String getSizeParameter() {
        return sizeParameter;
    }
    
    public void setSizeParameter(String sizeParameter) {
        this.sizeParameter = sizeParameter;
    }
    
    public String getSortParameter() {
        return sortParameter;
    }
    
    public void setSortParameter(String sortParameter) {
        this.sortParameter = sortParameter;
    }
}