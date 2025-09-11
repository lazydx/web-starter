package com.ldx.webstarter;

import com.ldx.webstarter.infrastructure.autoconfigure.WebStarterAutoConfiguration;
import com.ldx.webstarter.infrastructure.properties.WebStarterProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Web Starter 간단한 통합 테스트.
 * 
 * <p>ApplicationContextRunner를 사용하여 자동 설정이 정상적으로 
 * 로드되는지 확인합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class WebStarterSimpleIntegrationTest {
    
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebStarterAutoConfiguration.class));
    
    @Test
    @DisplayName("기본 설정으로 Web Starter가 로드된다")
    void defaultConfigurationLoads() {
        this.contextRunner.run(context -> {
            assertThat(context.getBeansOfType(WebStarterProperties.class)).hasSize(1);
            assertThat(context.getBeansOfType(WebStarterAutoConfiguration.class)).hasSize(1);
        });
    }
    
    @Test
    @DisplayName("프로퍼티가 올바르게 설정된다")
    void propertiesAreConfiguredCorrectly() {
        this.contextRunner
                .withPropertyValues(
                    "web-starter.enabled=true",
                    "web-starter.response.enabled=true",
                    "web-starter.cors.enabled=true",
                    "web-starter.pagination.enabled=true"
                )
                .run(context -> {
                    WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                    
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getResponse().isEnabled()).isTrue();
                    assertThat(properties.getCors().isEnabled()).isTrue();
                    assertThat(properties.getPagination().isEnabled()).isTrue();
                });
    }
    
    @Test
    @DisplayName("enabled=false일 때 자동 설정이 비활성화된다")
    void disabledConfiguration() {
        this.contextRunner
                .withPropertyValues("web-starter.enabled=false")
                .run(context -> {
                    assertThat(context.getBeansOfType(WebStarterAutoConfiguration.class)).isEmpty();
                    assertThat(context.getBeansOfType(WebStarterProperties.class)).isEmpty();
                });
    }
}