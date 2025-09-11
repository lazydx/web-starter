package com.ldx.webstarter.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Web Starter 루트 프로퍼티 클래스.
 * 
 * <p>web-starter의 모든 설정을 관리합니다.
 * Spring Boot Starter 표준 아키텍처 가이드라인을 준수합니다.
 * 
 * <p>핵심 기능:
 * - Mode 기반 아키텍처 지원 (Traditional/Hexagonal)
 * - FeatureToggle 기반 기능 제어
 * - Zero Configuration 원칙 준수
 * - 기존 설정과의 완전한 호환성
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "web-starter")
@Validated
public class WebStarterProperties {
    
    /**
     * 전체 스타터 활성화 여부
     * 기본값: true (Zero Configuration)
     */
    private Boolean enabled = true;
    
    /**
     * 아키텍처 모드
     * traditional: 모든 레이어에서 자유 사용
     * hexagonal: Domain Layer에서 사용 금지
     */
    private Mode mode = Mode.TRADITIONAL;
    
    /**
     * Response configuration.
     */
    @Valid
    @NestedConfigurationProperty
    private ResponseProperties response = new ResponseProperties();
    
    /**
     * CORS configuration.
     */
    @Valid
    @NestedConfigurationProperty
    private CorsProperties cors = new CorsProperties();
    
    /**
     * Pagination configuration.
     */
    @Valid
    @NestedConfigurationProperty
    private PaginationProperties pagination = new PaginationProperties();
    
    // === 표준 가이드라인 준수 FeatureToggle 구조 ===
    
    /**
     * 기능별 토글 - API 응답 표준화
     */
    @NestedConfigurationProperty
    private FeatureToggle responseToggle = new FeatureToggle(true);
    
    /**
     * 기능별 토글 - 전역 예외 처리
     */
    @NestedConfigurationProperty  
    private FeatureToggle exceptionToggle = new FeatureToggle(true);
    
    /**
     * 기능별 토글 - CORS 설정
     */
    @NestedConfigurationProperty
    private FeatureToggle corsToggle = new FeatureToggle(true);
    
    /**
     * 기능별 토글 - 파일 처리 (기본 OFF)
     */
    @NestedConfigurationProperty
    private FeatureToggle fileToggle = new FeatureToggle(false);
    
    /**
     * 기능별 토글 - 디버그 기능 (기본 OFF)
     */
    @NestedConfigurationProperty
    private FeatureToggle debugToggle = new FeatureToggle(false);
    
    public Boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
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
    
    // === Mode 관련 메서드 ===
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    // === FeatureToggle 관련 메서드 ===
    
    public FeatureToggle getResponseToggle() {
        return responseToggle;
    }
    
    public void setResponseToggle(FeatureToggle responseToggle) {
        this.responseToggle = responseToggle;
    }
    
    public FeatureToggle getExceptionToggle() {
        return exceptionToggle;
    }
    
    public void setExceptionToggle(FeatureToggle exceptionToggle) {
        this.exceptionToggle = exceptionToggle;
    }
    
    public FeatureToggle getCorsToggle() {
        return corsToggle;
    }
    
    public void setCorsToggle(FeatureToggle corsToggle) {
        this.corsToggle = corsToggle;
    }
    
    public FeatureToggle getFileToggle() {
        return fileToggle;
    }
    
    public void setFileToggle(FeatureToggle fileToggle) {
        this.fileToggle = fileToggle;
    }
    
    public FeatureToggle getDebugToggle() {
        return debugToggle;
    }
    
    public void setDebugToggle(FeatureToggle debugToggle) {
        this.debugToggle = debugToggle;
    }
    
    // === 편의 메서드들 (조건 로직을 Properties 내부로 이동) ===
    
    /**
     * API 응답 표준화 기능 활성화 여부
     * 루트 enabled && 기존 response.enabled && responseToggle.enabled 모두 확인
     */
    public boolean isResponseEnabled() {
        return enabled && response.isEnabled() && responseToggle.isEnabled();
    }
    
    /**
     * 전역 예외 처리 기능 활성화 여부
     * 루트 enabled && exceptionToggle.enabled 확인
     */
    public boolean isExceptionEnabled() {
        return enabled && exceptionToggle.isEnabled();
    }
    
    /**
     * CORS 설정 기능 활성화 여부
     * 루트 enabled && 기존 cors.enabled && corsToggle.enabled 모두 확인
     */
    public boolean isCorsEnabled() {
        return enabled && cors.isEnabled() && corsToggle.isEnabled();
    }
    
    /**
     * 파일 처리 기능 활성화 여부
     * 루트 enabled && fileToggle.enabled 확인
     */
    public boolean isFileEnabled() {
        return enabled && fileToggle.isEnabled();
    }
    
    /**
     * 디버그 기능 활성화 여부
     * 루트 enabled && debugToggle.enabled 확인
     */
    public boolean isDebugEnabled() {
        return enabled && debugToggle.isEnabled();
    }
    
    /**
     * 페이지네이션 기능 활성화 여부 (기존 호환성)
     */
    public boolean isPaginationEnabled() {
        return enabled && pagination != null;
    }
    
    // === 내부 클래스 정의 ===
    
    /**
     * 아키텍처 모드 정의
     */
    public enum Mode {
        /**
         * 전통적 MVC 아키텍처 - 모든 레이어에서 web-starter 자유 사용
         */
        TRADITIONAL,
        
        /**
         * 헥사고날 아키텍처 - Domain Layer에서 web-starter 사용 제한
         * Application Layer 이상에서만 사용 권장
         */
        HEXAGONAL
    }
    
    /**
     * 기능별 토글 클래스
     * 각 기능의 활성화/비활성화를 제어합니다.
     */
    public static class FeatureToggle {
        
        /**
         * 기능 활성화 여부
         */
        private boolean enabled;
        
        /**
         * 기본 생성자
         */
        public FeatureToggle() {
            this.enabled = false;
        }
        
        /**
         * 기본값 지정 생성자
         * @param defaultEnabled 기본 활성화 여부
         */
        public FeatureToggle(boolean defaultEnabled) {
            this.enabled = defaultEnabled;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        @Override
        public String toString() {
            return "FeatureToggle{enabled=" + enabled + "}";
        }
    }
}