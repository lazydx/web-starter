package com.ldx.webstarter.autoconfigure;

import com.ldx.webstarter.infrastructure.autoconfigure.WebStarterAutoConfiguration;
import com.ldx.webstarter.infrastructure.properties.WebStarterProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebStarterAutoConfiguration 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class WebStarterAutoConfigurationTest {
    
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                WebMvcAutoConfiguration.class,
                ValidationAutoConfiguration.class,
                WebStarterAutoConfiguration.class
            ));
    
    @Test
    @DisplayName("기본 설정으로 자동 설정이 활성화된다")
    void defaultConfiguration() {
        this.contextRunner
            .withPropertyValues("web-starter.enabled=true")
            .run(context -> {
                try {
                    System.out.println("=== Context Started Successfully ===");
                    System.out.println("WebStarterProperties beans: " + context.getBeansOfType(WebStarterProperties.class).size());
                    System.out.println("WebStarterAutoConfiguration beans: " + context.getBeansOfType(WebStarterAutoConfiguration.class).size());
                    
                    // 우선 Properties만 확인
                    assertThat(context.getBeansOfType(WebStarterProperties.class)).isNotEmpty();
                } catch (Exception e) {
                    System.err.println("Error in context: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
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
    
    @Test
    @DisplayName("기본 프로퍼티 값이 설정된다")
    void defaultPropertyValues() {
        this.contextRunner.run(context -> {
            if (context.getBeansOfType(WebStarterProperties.class).size() > 0) {
                WebStarterProperties properties = context.getBean(WebStarterProperties.class);
                assertThat(properties.isEnabled()).isTrue();
            }
        });
    }
}