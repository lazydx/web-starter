package com.ldx.webstarter.infrastructure.autoconfigure;

import com.ldx.webstarter.infrastructure.resolver.PaginationArgumentResolver;
import com.ldx.webstarter.infrastructure.properties.PaginationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 예외 처리 및 페이지네이션 자동 설정 클래스.
 * 
 * <p>전역 예외 처리기와 페이지네이션 인수 리졸버를 자동 설정합니다.
 * 일관된 에러 응답과 페이지네이션 제한을 제공합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(PaginationProperties.class)
public class ExceptionAutoConfiguration {
    
    
    /**
     * 페이지네이션 리졸버를 등록하는 WebMvcConfigurer 빈을 생성합니다.
     * Spring Data의 기본 PageableHandlerMethodArgumentResolver보다 우선순위를 높여 등록합니다.
     * 
     * @param paginationProperties 페이지네이션 관련 프로퍼티
     * @return WebMvcConfigurer 인스턴스
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
    public WebMvcConfigurer paginationConfigurer(PaginationProperties paginationProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                // 기존 PageableHandlerMethodArgumentResolver보다 먼저 추가하여 우선순위 확보
                resolvers.add(0, new PaginationArgumentResolver(paginationProperties));
            }
        };
    }
}