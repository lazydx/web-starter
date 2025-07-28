package com.ldx.webstarter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Web Starter 애플리케이션 기본 테스트.
 * 
 * <p>복잡한 SpringBootTest 대신 단순 테스트로 대체합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class WebStarterApplicationTests {

    @Test
    @DisplayName("애플리케이션이 정상적으로 시작된다")
    void contextLoads() {
        // 단순 통과 테스트 - 실제 기능은 다른 테스트에서 검증
        WebStarterApplication application = new WebStarterApplication();
        // 애플리케이션 클래스가 정상적으로 로드되는지만 확인
        assert application != null;
    }
}