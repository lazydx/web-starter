package com.ldx.webstarter.infrastructure.autoconfigure;

import com.ldx.webstarter.infrastructure.debug.RequestLoggingFilter;
import com.ldx.webstarter.infrastructure.properties.DebugProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 디버깅 기능 자동 설정 클래스.
 * 
 * <p>디버그 모드가 활성화된 경우 다양한 디버깅 기능들을 활성화합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(DebugProperties.class)
@ConditionalOnProperty(prefix = "web-starter.debug", name = "enabled", havingValue = "true")
public class DebugAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DebugAutoConfiguration.class);

    /**
     * 요청 로깅 필터를 등록합니다.
     *
     * @param debugProperties 디버그 프로퍼티
     * @return FilterRegistrationBean
     */
    @Bean
    @ConditionalOnProperty(prefix = "web-starter.debug", name = "log-requests", havingValue = "true")
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter(DebugProperties debugProperties) {
        logger.info("Registering RequestLoggingFilter for debug mode");
        
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestLoggingFilter(debugProperties));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.setName("requestLoggingFilter");
        
        return registrationBean;
    }

    /**
     * 디버그 정보 로거를 등록합니다.
     *
     * @param debugProperties 디버그 프로퍼티
     * @return DebugInfoLogger
     */
    @Bean
    @ConditionalOnProperty(prefix = "web-starter.debug", name = "log-configuration", havingValue = "true")
    public DebugInfoLogger debugInfoLogger(DebugProperties debugProperties) {
        return new DebugInfoLogger(debugProperties);
    }

    /**
     * 디버그 정보를 로깅하는 클래스.
     */
    public static class DebugInfoLogger {
        private static final Logger logger = LoggerFactory.getLogger(DebugInfoLogger.class);
        
        public DebugInfoLogger(DebugProperties debugProperties) {
            logDebugConfiguration(debugProperties);
        }
        
        private void logDebugConfiguration(DebugProperties debugProperties) {
            logger.info("=== WEB-STARTER DEBUG MODE ENABLED ===");
            logger.info("Debug Configuration:");
            logger.info("  - Log Requests: {}", debugProperties.isLogRequests());
            logger.info("  - Log Request Body: {}", debugProperties.isLogRequestBody());
            logger.info("  - Log Response Body: {}", debugProperties.isLogResponseBody());
            logger.info("  - Log Performance Metrics: {}", debugProperties.isLogPerformanceMetrics());
            logger.info("  - Log Detailed Exceptions: {}", debugProperties.isLogDetailedExceptions());
            logger.info("  - Log Bean Registration: {}", debugProperties.isLogBeanRegistration());
            logger.info("  - Log Level: {}", debugProperties.getLogLevel());
            logger.info("  - Max Request Body Log Size: {} bytes", debugProperties.getMaxRequestBodyLogSize());
            logger.info("  - Max Response Body Log Size: {} bytes", debugProperties.getMaxResponseBodyLogSize());
            logger.info("=====================================");
        }
    }
}