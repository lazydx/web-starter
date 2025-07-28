package com.ldx.webstarter.infrastructure.autoconfigure;

import com.ldx.webstarter.infrastructure.properties.WebStarterProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Starter 메인 자동 설정 클래스.
 * 
 * <p>web-starter의 모든 기능을 통합 관리하는 메인 자동 설정입니다.
 * 각 기능별 자동 설정 클래스들을 임포트하여 전체적인 설정을 조율합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "web-starter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(WebStarterProperties.class)
@Import({
    ResponseAutoConfiguration.class,
    CorsAutoConfiguration.class,
    ExceptionAutoConfiguration.class
})
public class WebStarterAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(WebStarterAutoConfiguration.class);
    
    private final WebStarterProperties properties;
    
    public WebStarterAutoConfiguration(WebStarterProperties properties) {
        this.properties = properties;
    }
    
    /**
     * 자동 설정 초기화 시 로깅을 수행합니다.
     */
    @PostConstruct
    public void init() {
        logger.info("Web Starter auto-configuration initialized with enabled: {}", properties.isEnabled());
    }
}