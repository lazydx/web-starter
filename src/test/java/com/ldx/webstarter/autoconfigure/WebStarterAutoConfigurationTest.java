package com.ldx.webstarter.autoconfigure;

import com.ldx.webstarter.infrastructure.autoconfigure.WebStarterAutoConfiguration;
import com.ldx.webstarter.infrastructure.properties.WebStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebStarterAutoConfiguration 테스트
 * 
 * Web Starter의 핵심 Auto-Configuration 동작을 검증합니다.
 */
class WebStarterAutoConfigurationTest {
    
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            WebStarterAutoConfiguration.class,
            // 필요한 의존성 Auto-Configuration 추가
            org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
            org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
        ));
    
    @Test
    void zeroConfiguration_shouldStartWithDefaults() {
        // Zero Configuration: 아무 설정 없이도 시작 가능해야 함
        contextRunner.run(context -> {
            assertThat(context).isNotNull();
            assertThat(context.isActive()).isTrue();
            
            // Properties가 기본값으로 생성되어야 함
            assertThat(context).hasSingleBean(WebStarterProperties.class);
            
            WebStarterProperties properties = context.getBean(WebStarterProperties.class);
            assertThat(properties).isNotNull();
            assertThat(properties.isEnabled()).isTrue(); // matchIfMissing = true
        });
    }
    
    @Test
    void whenDisabled_shouldNotCreateBeans() {
        // web-starter.enabled=false 시 AutoConfiguration 자체가 로드되지 않음
        contextRunner
            .withPropertyValues("web-starter.enabled=false")
            .run(context -> {
                assertThat(context).isNotNull();
                
                // AutoConfiguration이 로드되지 않으므로 Properties 빈도 생성되지 않음
                assertThat(context).doesNotHaveBean(WebStarterProperties.class);
                assertThat(context).doesNotHaveBean(WebStarterAutoConfiguration.class);
            });
    }
    
    @Test
    void properties_shouldBindCorrectly() {
        // Properties 바인딩 테스트
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.mode=HEXAGONAL",
                "web-starter.response-toggle.enabled=true",
                "web-starter.exception-toggle.enabled=true",
                "web-starter.cors-toggle.enabled=false",
                "web-starter.file-toggle.enabled=true",
                "web-starter.debug-toggle.enabled=false"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                
                assertThat(properties.isEnabled()).isTrue();
                assertThat(properties.getMode()).isEqualTo(WebStarterProperties.Mode.HEXAGONAL);
                assertThat(properties.isResponseEnabled()).isTrue();
                assertThat(properties.isExceptionEnabled()).isTrue();
                assertThat(properties.isCorsEnabled()).isFalse();
                assertThat(properties.isFileEnabled()).isTrue();
                assertThat(properties.isDebugEnabled()).isFalse();
            });
    }
    
    @Test
    void featureToggle_response_shouldWorkCorrectly() {
        // Response Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.response-toggle.enabled=true"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isResponseEnabled()).isTrue();
            });
        
        // Response 비활성화
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.response-toggle.enabled=false"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isResponseEnabled()).isFalse();
            });
    }
    
    @Test
    void featureToggle_exception_shouldWorkCorrectly() {
        // Exception Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.exception-toggle.enabled=true"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isExceptionEnabled()).isTrue();
            });
    }
    
    @Test
    void featureToggle_cors_shouldWorkCorrectly() {
        // CORS Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.cors-toggle.enabled=true",
                "web-starter.cors.allowed-origins=http://localhost:3000",
                "web-starter.cors.allowed-methods=GET,POST"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isCorsEnabled()).isTrue();
                
                // CORS 설정 확인
                assertThat(properties.getCors()).isNotNull();
            });
    }
    
    @Test
    void featureToggle_file_shouldWorkCorrectly() {
        // File Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.file-toggle.enabled=true",
                "web-starter.file.upload-dir=/tmp/uploads",
                "web-starter.file.max-file-size=10MB"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isFileEnabled()).isTrue();
                
                // File Toggle 확인
                assertThat(properties.getFileToggle()).isNotNull();
                assertThat(properties.getFileToggle().isEnabled()).isTrue();
            });
    }
    
    @Test
    void featureToggle_debug_shouldWorkCorrectly() {
        // Debug Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.debug-toggle.enabled=true",
                "web-starter.debug.log-request=true",
                "web-starter.debug.log-response=true"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isDebugEnabled()).isTrue();
                
                // Debug Toggle 확인
                assertThat(properties.getDebugToggle()).isNotNull();
                assertThat(properties.getDebugToggle().isEnabled()).isTrue();
            });
    }
    
    @Test
    void mode_traditional_shouldBeDefault() {
        // 기본 Mode는 TRADITIONAL이어야 함
        contextRunner.run(context -> {
            WebStarterProperties properties = context.getBean(WebStarterProperties.class);
            assertThat(properties.getMode()).isEqualTo(WebStarterProperties.Mode.TRADITIONAL);
        });
    }
    
    @Test
    void mode_hexagonal_shouldBeConfigurable() {
        // HEXAGONAL Mode 설정 가능
        contextRunner
            .withPropertyValues("web-starter.mode=HEXAGONAL")
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.getMode()).isEqualTo(WebStarterProperties.Mode.HEXAGONAL);
            });
    }
    
    @Test
    void allFeatures_canBeEnabledTogether() {
        // 모든 기능을 동시에 활성화 가능
        contextRunner
            .withPropertyValues(
                "web-starter.enabled=true",
                "web-starter.response-toggle.enabled=true",
                "web-starter.exception-toggle.enabled=true",
                "web-starter.cors-toggle.enabled=true",
                "web-starter.file-toggle.enabled=true",
                "web-starter.debug-toggle.enabled=true"
            )
            .run(context -> {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                
                assertThat(properties.isResponseEnabled()).isTrue();
                assertThat(properties.isExceptionEnabled()).isTrue();
                assertThat(properties.isCorsEnabled()).isTrue();
                assertThat(properties.isFileEnabled()).isTrue();
                assertThat(properties.isDebugEnabled()).isTrue();
            });
    }
}