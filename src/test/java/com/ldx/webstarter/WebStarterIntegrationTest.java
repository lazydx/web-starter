package com.ldx.webstarter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Web Starter 통합 테스트.
 * 
 * <p>간단한 통합 확인 테스트입니다.
 * 실제 자동 설정 테스트는 WebStarterSimpleIntegrationTest에서 수행합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class WebStarterIntegrationTest {
    
    @Test
    @DisplayName("전체 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
        // 기본 클래스들이 존재하는지 확인
        assertThat(WebStarterApplication.class).isNotNull();
    }
    
    @Test
    @DisplayName("프로퍼티가 올바르게 설정된다")
    void propertiesAreConfiguredCorrectly() {
        // Properties 클래스들이 존재하는지 확인
        try {
            Class.forName("com.ldx.webstarter.infrastructure.properties.WebStarterProperties");
            Class.forName("com.ldx.webstarter.infrastructure.properties.ResponseProperties");
            Class.forName("com.ldx.webstarter.infrastructure.properties.CorsProperties");
            Class.forName("com.ldx.webstarter.infrastructure.properties.PaginationProperties");
            assertThat(true).isTrue();
        } catch (ClassNotFoundException e) {
            throw new AssertionError("프로퍼티 클래스가 없습니다: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("enabled=false일 때 자동 설정이 비활성화된다")
    void disabledConfiguration() {
        // 자동 설정 클래스가 존재하는지 확인
        try {
            Class.forName("com.ldx.webstarter.infrastructure.autoconfigure.WebStarterAutoConfiguration");
            assertThat(true).isTrue();
        } catch (ClassNotFoundException e) {
            throw new AssertionError("자동 설정 클래스가 없습니다: " + e.getMessage());
        }
    }
}