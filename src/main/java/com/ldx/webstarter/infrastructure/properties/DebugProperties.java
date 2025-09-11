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
    
    /**
     * 성능 모니터링 설정.
     */
    private Performance performance = new Performance();

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

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    /**
     * 성능 모니터링 설정 클래스.
     */
    public static class Performance {

        /**
         * 성능 메트릭 로깅 활성화 여부.
         */
        private boolean enabled = true;

        /**
         * 느린 요청 임계값 (밀리초).
         * 이 값을 초과하는 요청은 WARN 레벨로 "SLOW REQUEST DETECTED" 메시지가 로깅됩니다.
         * 기본값: 5000ms (5초)
         */
        private long slowRequestThreshold = 5000L;

        /**
         * 성능 경고 임계값 (밀리초).
         * 이 값을 초과하지만 slowRequestThreshold는 넘지 않는 요청은 WARN 레벨로 "Performance Alert" 메시지가 로깅됩니다.
         * 기본값: 1000ms (1초)
         */
        private long performanceAlertThreshold = 1000L;

        /**
         * 성능 메트릭 수집 활성화 여부.
         * false로 설정하면 성능 메트릭 계산 자체를 건너뜁니다.
         */
        private boolean collectMetrics = true;

        /**
         * 요청 크기 임계값 (바이트).
         * 이 값을 초과하는 요청 크기에 대해서는 별도 로깅됩니다.
         * 기본값: 1MB
         */
        private long largeRequestSizeThreshold = 1024 * 1024L; // 1MB

        /**
         * 응답 크기 임계값 (바이트).
         * 이 값을 초과하는 응답 크기에 대해서는 별도 로깅됩니다.
         * 기본값: 1MB
         */
        private long largeResponseSizeThreshold = 1024 * 1024L; // 1MB

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getSlowRequestThreshold() {
            return slowRequestThreshold;
        }

        public void setSlowRequestThreshold(long slowRequestThreshold) {
            this.slowRequestThreshold = slowRequestThreshold;
        }

        public long getPerformanceAlertThreshold() {
            return performanceAlertThreshold;
        }

        public void setPerformanceAlertThreshold(long performanceAlertThreshold) {
            this.performanceAlertThreshold = performanceAlertThreshold;
        }

        public boolean isCollectMetrics() {
            return collectMetrics;
        }

        public void setCollectMetrics(boolean collectMetrics) {
            this.collectMetrics = collectMetrics;
        }

        public long getLargeRequestSizeThreshold() {
            return largeRequestSizeThreshold;
        }

        public void setLargeRequestSizeThreshold(long largeRequestSizeThreshold) {
            this.largeRequestSizeThreshold = largeRequestSizeThreshold;
        }

        public long getLargeResponseSizeThreshold() {
            return largeResponseSizeThreshold;
        }

        public void setLargeResponseSizeThreshold(long largeResponseSizeThreshold) {
            this.largeResponseSizeThreshold = largeResponseSizeThreshold;
        }
    }
}