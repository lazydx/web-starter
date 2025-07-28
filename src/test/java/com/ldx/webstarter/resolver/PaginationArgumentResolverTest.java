package com.ldx.webstarter.resolver;

import com.ldx.webstarter.infrastructure.properties.PaginationProperties;
import com.ldx.webstarter.infrastructure.resolver.PaginationArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PaginationArgumentResolver 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class PaginationArgumentResolverTest {
    
    private PaginationArgumentResolver resolver;
    private PaginationProperties properties;
    
    @BeforeEach
    void setUp() {
        properties = new PaginationProperties();
        resolver = new PaginationArgumentResolver(properties);
    }
    
    @Test
    @DisplayName("Pageable 파라미터를 지원한다")
    void supportsPageableParameter() {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getParameterType()).thenAnswer(invocation -> Pageable.class);
        
        boolean supports = resolver.supportsParameter(parameter);
        
        assertThat(supports).isTrue();
    }
    
    @Test
    @DisplayName("Pageable이 아닌 파라미터는 지원하지 않는다")
    void doesNotSupportNonPageableParameter() {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getParameterType()).thenAnswer(invocation -> String.class);
        
        boolean supports = resolver.supportsParameter(parameter);
        
        assertThat(supports).isFalse();
    }
    
    @Test
    @DisplayName("기본 페이지네이션 파라미터를 리졸브한다")
    void resolvesDefaultPaginationParameters() throws Exception {
        MethodParameter parameter = mock(MethodParameter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);
        
        Object result = resolver.resolveArgument(parameter, null, webRequest, null);
        
        assertThat(result).isInstanceOf(Pageable.class);
        Pageable pageable = (Pageable) result;
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20); // 기본값
    }
    
    @Test
    @DisplayName("커스텀 페이지네이션 파라미터를 리졸브한다")
    void resolvesCustomPaginationParameters() throws Exception {
        MethodParameter parameter = mock(MethodParameter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("page", "2");
        request.setParameter("size", "15");
        ServletWebRequest webRequest = new ServletWebRequest(request);
        
        Object result = resolver.resolveArgument(parameter, null, webRequest, null);
        
        assertThat(result).isInstanceOf(Pageable.class);
        Pageable pageable = (Pageable) result;
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(15);
    }
    
    @Test
    @DisplayName("최대 페이지 크기 제한을 적용한다")
    void appliesMaxSizeLimit() throws Exception {
        MethodParameter parameter = mock(MethodParameter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("page", "0");
        request.setParameter("size", "150"); // 최대값 100 초과
        ServletWebRequest webRequest = new ServletWebRequest(request);
        
        Object result = resolver.resolveArgument(parameter, null, webRequest, null);
        
        assertThat(result).isInstanceOf(Pageable.class);
        Pageable pageable = (Pageable) result;
        assertThat(pageable.getPageSize()).isEqualTo(100); // 최대값으로 제한
    }
    
    @Test
    @DisplayName("음수 페이지 번호를 0으로 조정한다")
    void adjustsNegativePageNumberToZero() throws Exception {
        MethodParameter parameter = mock(MethodParameter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("page", "-1");
        request.setParameter("size", "20");
        ServletWebRequest webRequest = new ServletWebRequest(request);
        
        Object result = resolver.resolveArgument(parameter, null, webRequest, null);
        
        assertThat(result).isInstanceOf(Pageable.class);
        Pageable pageable = (Pageable) result;
        assertThat(pageable.getPageNumber()).isEqualTo(0);
    }
}