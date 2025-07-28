package com.ldx.webstarter;

import com.ldx.webstarter.infrastructure.autoconfigure.WebStarterAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 테스트용 설정 클래스.
 * 
 * <p>테스트에서 Web Starter 자동 설정을 강제로 활성화합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@Configuration
@ImportAutoConfiguration({
    WebMvcAutoConfiguration.class,
    WebStarterAutoConfiguration.class
})
public class TestConfiguration {
}