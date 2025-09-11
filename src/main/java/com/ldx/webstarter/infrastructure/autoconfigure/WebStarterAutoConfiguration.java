package com.ldx.webstarter.infrastructure.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.webstarter.file.*;
import com.ldx.webstarter.infrastructure.debug.RequestLoggingFilter;
import com.ldx.webstarter.infrastructure.file.DefaultFileNameGenerator;
import com.ldx.webstarter.infrastructure.file.FileNameGenerator;
import com.ldx.webstarter.infrastructure.file.FileNamingProperties;
import com.ldx.webstarter.infrastructure.properties.*;
import com.ldx.webstarter.infrastructure.resolver.PaginationArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Web Starter 메인 자동 설정 클래스.
 * 
 * <p>Spring Boot Starter 표준 아키텍처 가이드라인을 준수합니다.
 * 
 * <p>핵심 원칙:
 * 1. 단일 조건만 사용 (@ConditionalOnProperty 1개)
 * 2. 모든 Bean은 @ConditionalOnMissingBean (부모 우선)
 * 3. 조건 로직은 Properties 편의 메서드에서 처리
 * 4. Mode 기반 아키텍처 지원 (Traditional/Hexagonal)
 * 5. Hybrid 접근: MVC 컴포넌트는 @ComponentScan, Service Bean은 명시적 등록
 * 
 * @author web-starter
 * @since 1.0.0
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnProperty(
    prefix = "web-starter", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true  // Zero Configuration 원칙
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({
    WebStarterProperties.class,
    ResponseProperties.class,
    CorsProperties.class, 
    PaginationProperties.class,
    FileStorageProperties.class,
    FileNamingProperties.class,
    DebugProperties.class
})
@ComponentScan(basePackages = {
    "com.ldx.webstarter.file",                      // File Controllers
    "com.ldx.webstarter.infrastructure.advice",     // ResponseAdvice  
    "com.ldx.webstarter.infrastructure.exception",  // GlobalExceptionHandler
    "com.ldx.webstarter"                            // TestController (테스트용)
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
        // 헤더
        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║                 🚀 LDX WEB STARTER                            ║");
        logger.info("║                    Version 1.1.0                               ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");
        
        // 설정 상태
        logger.info("┌─── Configuration Status ─────────────────────────────────────┐");
        logger.info("│ ✅ Enabled: {}", properties.isEnabled());
        logger.info("│ 📋 Mode: {}", properties.getMode());
        logger.info("│ 🔧 Prefix: web-starter");
        logger.info("└──────────────────────────────────────────────────────────────┘");
        
        // 활성화된 기능
        logger.info("┌─── Active Features ──────────────────────────────────────────┐");
        logger.info("│ {} Response Standardization", properties.isResponseEnabled() ? "✅" : "❌");
        logger.info("│ {} Global Exception Handler", properties.isExceptionEnabled() ? "✅" : "❌");
        logger.info("│ {} CORS Configuration", properties.isCorsEnabled() ? "✅" : "❌");
        logger.info("│ {} File Upload/Download", properties.isFileEnabled() ? "✅" : "❌");
        logger.info("│ {} Debug Mode", properties.isDebugEnabled() ? "✅" : "❌");
        logger.info("└──────────────────────────────────────────────────────────────┘");
        
        // 완료 메시지
        logger.info("✅ Web Starter initialization complete");
    }
    
    // === MVC 컴포넌트들은 @ComponentScan으로 자동 등록됨 ===
    // GlobalExceptionHandler (@RestControllerAdvice)
    // ResponseAdvice (@RestControllerAdvice)  
    // FileUploadController (@RestController)
    // FileDownloadController (@RestController)
    
    // === 통합 WebMvcConfigurer Bean ===
    
    /**
     * 모든 WebMvc 설정을 통합한 단일 WebMvcConfigurer Bean
     * HttpMessageConverter, CORS, Pagination 설정을 모두 처리
     */
    @Bean
    @ConditionalOnMissingBean(name = "webStarterWebMvcConfigurer")
    public WebMvcConfigurer webStarterWebMvcConfigurer(
            CorsProperties corsProperties, 
            PaginationProperties paginationProperties) {
        
        return new WebMvcConfigurer() {
            
            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                if (!properties.isResponseEnabled()) {
                    return;
                }
                
                logger.debug("Configuring HttpMessageConverter order for ResponseAdvice compatibility");
                
                // StringHttpMessageConverter를 찾아서 제거
                HttpMessageConverter<?> stringConverter = null;
                for (int i = 0; i < converters.size(); i++) {
                    if (converters.get(i) instanceof StringHttpMessageConverter) {
                        stringConverter = converters.remove(i);
                        break;
                    }
                }
                
                // MappingJackson2HttpMessageConverter 뒤에 StringHttpMessageConverter 추가
                if (stringConverter != null) {
                    boolean jacksonFound = false;
                    for (int i = 0; i < converters.size(); i++) {
                        if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                            converters.add(i + 1, stringConverter);
                            jacksonFound = true;
                            break;
                        }
                    }
                    
                    // Jackson 컨버터가 없으면 마지막에 추가
                    if (!jacksonFound) {
                        converters.add(stringConverter);
                    }
                }
            }
            
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (!properties.isCorsEnabled()) {
                    return;
                }
                
                logger.info("web-starter CORS configuration initialized");
                registry.addMapping(corsProperties.getPathPattern())
                        .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                        .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                        .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                        .allowCredentials(corsProperties.isAllowCredentials())
                        .maxAge(corsProperties.getMaxAge().toSeconds());
            }
            
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                if (!properties.isPaginationEnabled()) {
                    return;
                }
                
                logger.info("web-starter pagination resolver initialized");
                try {
                    // 기존 PageableHandlerMethodArgumentResolver보다 먼저 추가하여 우선순위 확보
                    resolvers.add(0, new PaginationArgumentResolver(paginationProperties));
                } catch (Exception e) {
                    // Spring Data 의존성이 없는 경우 무시
                    logger.debug("Spring Data not available, skipping pagination resolver");
                }
            }
        };
    }
    
    // === CORS 설정 Bean ===
    
    /**
     * CORS 설정 소스 Bean
     */
    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    @ConditionalOnProperty(prefix = "web-starter.cors", name = "enabled", havingValue = "true")
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge().toSeconds());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPathPattern(), configuration);
        return source;
    }
    
    // === 파일 처리 Service Bean들 (여전히 명시적 등록 필요) ===
    
    /**
     * 파일명 생성기 Bean
     */
    @Bean
    @ConditionalOnMissingBean(FileNameGenerator.class)
    @ConditionalOnProperty(prefix = "web-starter.file-storage", name = "enabled", havingValue = "true")
    public FileNameGenerator fileNameGenerator(FileNamingProperties properties) {
        logger.info("web-starter file name generator initialized");
        return new DefaultFileNameGenerator(properties);
    }
    
    /**
     * 파일 검증 서비스 Bean
     */
    @Bean
    @ConditionalOnMissingBean(FileValidationService.class)
    @ConditionalOnProperty(prefix = "web-starter.file-storage", name = "enabled", havingValue = "true")
    public FileValidationService fileValidationService(FileStorageProperties properties) {
        logger.info("web-starter file validation service initialized");
        return new FileValidationService(properties);
    }
    
    /**
     * 파일 저장 서비스 Bean
     */
    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    @ConditionalOnProperty(prefix = "web-starter.file-storage", name = "enabled", havingValue = "true")
    public FileStorageService localFileStorageService(FileStorageProperties properties, FileNameGenerator fileNameGenerator) {
        logger.info("web-starter local file storage service initialized");
        return new LocalFileStorageService(properties, fileNameGenerator);
    }
    
    // === 디버그 기능 Bean들 ===
    
    /**
     * 요청 로깅 필터 Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "requestLoggingFilter")
    @ConditionalOnProperty(prefix = "web-starter.debug", name = "log-requests", havingValue = "true")
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter(DebugProperties debugProperties) {
        logger.info("web-starter request logging filter initialized");
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestLoggingFilter(debugProperties));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.setName("requestLoggingFilter");
        
        return registrationBean;
    }
    
    /**
     * 디버그 정보 로거 Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "debugInfoLogger")
    @ConditionalOnProperty(prefix = "web-starter.debug", name = "log-configuration", havingValue = "true")
    public DebugInfoLogger debugInfoLogger(DebugProperties debugProperties) {
        logger.info("web-starter debug info logger initialized");
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
            logger.info("=======================================");
        }
    }
}