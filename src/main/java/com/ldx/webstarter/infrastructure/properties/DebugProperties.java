package com.ldx.webstarter.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 디버깅 및 개발 지원 프로퍼티 클래스.
 * 
 * <p>개발 환경에서 유용한 디버깅 기능들을 제어합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "web-starter.debug")
public class DebugProperties {
    
    /**
     * 디버그 모드 활성화 여부.
     */
    private boolean enabled = false;
    
    /**
     * 요청/응답 로깅 활성화 여부.
     */
    private boolean logRequests = false;
    
    /**
     * 요청 본문 로깅 활성화 여부.
     */
    private boolean logRequestBody = false;
    
    /**
     * 응답 본문 로깅 활성화 여부.
     */
    private boolean logResponseBody = false;
    
    /**
     * 성능 메트릭 로깅 활성화 여부.
     */
    private boolean logPerformanceMetrics = false;
    
    /**
     * 상세 예외 스택 트레이스 로깅 여부.
     */
    private boolean logDetailedExceptions = false;
    
    /**
     * 빈 등록 정보 로깅 여부.
     */
    private boolean logBeanRegistration = false;
    
    /**
     * 구성 설정 로깅 여부.
     */
    private boolean logConfiguration = false;
    
    /**
     * 로그 레벨.
     */
    private String logLevel = "DEBUG";
    
    /**
     * 최대 요청 본문 로그 크기 (바이트).
     */
    private int maxRequestBodyLogSize = 1024;
    
    /**
     * 최대 응답 본문 로그 크기 (바이트).
     */
    private int maxResponseBodyLogSize = 1024;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogRequests() {
        return logRequests;
    }

    public void setLogRequests(boolean logRequests) {
        this.logRequests = logRequests;
    }

    public boolean isLogRequestBody() {
        return logRequestBody;
    }

    public void setLogRequestBody(boolean logRequestBody) {
        this.logRequestBody = logRequestBody;
    }

    public boolean isLogResponseBody() {
        return logResponseBody;
    }

    public void setLogResponseBody(boolean logResponseBody) {
        this.logResponseBody = logResponseBody;
    }

    public boolean isLogPerformanceMetrics() {
        return logPerformanceMetrics;
    }

    public void setLogPerformanceMetrics(boolean logPerformanceMetrics) {
        this.logPerformanceMetrics = logPerformanceMetrics;
    }

    public boolean isLogDetailedExceptions() {
        return logDetailedExceptions;
    }

    public void setLogDetailedExceptions(boolean logDetailedExceptions) {
        this.logDetailedExceptions = logDetailedExceptions;
    }

    public boolean isLogBeanRegistration() {
        return logBeanRegistration;
    }

    public void setLogBeanRegistration(boolean logBeanRegistration) {
        this.logBeanRegistration = logBeanRegistration;
    }

    public boolean isLogConfiguration() {
        return logConfiguration;
    }

    public void setLogConfiguration(boolean logConfiguration) {
        this.logConfiguration = logConfiguration;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public int getMaxRequestBodyLogSize() {
        return maxRequestBodyLogSize;
    }

    public void setMaxRequestBodyLogSize(int maxRequestBodyLogSize) {
        this.maxRequestBodyLogSize = maxRequestBodyLogSize;
    }

    public int getMaxResponseBodyLogSize() {
        return maxResponseBodyLogSize;
    }

    public void setMaxResponseBodyLogSize(int maxResponseBodyLogSize) {
        this.maxResponseBodyLogSize = maxResponseBodyLogSize;
    }
}