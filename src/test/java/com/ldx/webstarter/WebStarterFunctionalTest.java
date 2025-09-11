package com.ldx.webstarter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.webstarter.infrastructure.advice.ResponseAdvice;
import com.ldx.webstarter.infrastructure.exception.BusinessException;
import com.ldx.webstarter.infrastructure.exception.GlobalExceptionHandler;
import com.ldx.webstarter.infrastructure.properties.PaginationProperties;
import com.ldx.webstarter.infrastructure.properties.WebStarterProperties;
import com.ldx.webstarter.infrastructure.resolver.PaginationArgumentResolver;
import com.ldx.webstarter.response.ApiResponse;
import com.ldx.webstarter.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Web Starter 기능별 테스트.
 * 
 * <p>Spring Boot 컨텍스트 없이 각 컴포넌트의 기능을 개별적으로 테스트합니다.
 * 이를 통해 핵심 로직이 올바르게 작동하는지 확인합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class WebStarterFunctionalTest {
    
    private ResponseAdvice responseAdvice;
    private GlobalExceptionHandler exceptionHandler;
    private PaginationArgumentResolver paginationArgumentResolver;
    
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        responseAdvice = new ResponseAdvice(objectMapper);
        
        // Mock WebStarterProperties for GlobalExceptionHandler
        WebStarterProperties properties = new WebStarterProperties();
        exceptionHandler = new GlobalExceptionHandler(properties);
        
        PaginationProperties paginationProperties = new PaginationProperties();
        paginationArgumentResolver = new PaginationArgumentResolver(paginationProperties);
    }
    
    @Test
    @DisplayName("ResponseAdvice가 일반 응답을 ApiResponse로 래핑한다")
    void responseAdviceWrapsNormalResponse() {
        String originalData = "Hello World";
        
        // Mock MethodParameter for String return type
        MethodParameter returnType = mock(MethodParameter.class);
        doReturn(String.class).when(returnType).getParameterType();
        doReturn(TestController.class).when(returnType).getDeclaringClass();
        
        // Mock ServerHttpResponse with HttpHeaders
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        doReturn(headers).when(response).getHeaders();
        
        Object result = responseAdvice.beforeBodyWrite(
            originalData,
            returnType,
            MediaType.APPLICATION_JSON,
            null,
            mock(ServerHttpRequest.class),
            response
        );
        
        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<String> apiResponse = (ApiResponse<String>) result;
        assertThat(apiResponse.isSuccess()).isTrue();
        assertThat(apiResponse.getData()).isEqualTo(originalData);
        assertThat(apiResponse.getTimestamp()).isNotNull();
        assertThat(apiResponse.getRequestId()).isNotNull();
    }
    
    // Test controller class for mocking
    static class TestController {}
    
    @Test
    @DisplayName("ResponseAdvice가 ApiResponse를 그대로 반환한다")
    void responseAdviceReturnsApiResponseAsIs() {
        ApiResponse<String> originalResponse = ApiResponse.success("test");
        
        // Mock MethodParameter for ApiResponse return type
        MethodParameter returnType = mock(MethodParameter.class);
        doReturn(ApiResponse.class).when(returnType).getParameterType();
        doReturn(TestController.class).when(returnType).getDeclaringClass();
        
        // Mock ServerHttpResponse with HttpHeaders
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        doReturn(headers).when(response).getHeaders();
        
        Object result = responseAdvice.beforeBodyWrite(
            originalResponse,
            returnType,
            MediaType.APPLICATION_JSON,
            null,
            mock(ServerHttpRequest.class),
            response
        );
        
        assertThat(result).isSameAs(originalResponse);
    }
    
    @Test
    @DisplayName("GlobalExceptionHandler가 BusinessException을 400으로 처리한다")
    void exceptionHandlerHandlesBusinessException() {
        BusinessException exception = new BusinessException("BUSINESS_ERROR", "Test business error");
        
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();
        assertThat(response.getBody().getError().getCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(response.getBody().getError().getMessage()).isEqualTo("Test business error");
    }
    
    @Test
    @DisplayName("GlobalExceptionHandler가 일반 예외를 500으로 처리한다")
    void exceptionHandlerHandlesGeneralException() {
        RuntimeException exception = new RuntimeException("General error");
        
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);
        
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isNotNull();
        assertThat(response.getBody().getError().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getError().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }
    
    @Test
    @DisplayName("PaginationArgumentResolver가 기본 페이지네이션을 생성한다")
    void paginationArgumentResolverCreatesDefaultPagination() throws Exception {
        MethodParameter parameter = mock(MethodParameter.class);
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        
        Object result = paginationArgumentResolver.resolveArgument(parameter, null, webRequest, null);
        
        assertThat(result).isInstanceOf(Pageable.class);
        Pageable pageable = (Pageable) result;
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }
    
    @Test
    @DisplayName("PageResponse가 올바르게 생성된다")
    void pageResponseIsCreatedCorrectly() {
        List<String> content = List.of("item1", "item2", "item3");
        int size = 20;
        int number = 0;
        long totalElements = 100L;
        int totalPages = 5;
        
        PageResponse<String> pageResponse = PageResponse.of(content, size, number, totalElements, totalPages);
        
        assertThat(pageResponse.getContent()).hasSize(3);
        assertThat(pageResponse.getContent()).containsExactly("item1", "item2", "item3");
        assertThat(pageResponse.getPage().getSize()).isEqualTo(20);
        assertThat(pageResponse.getPage().getNumber()).isEqualTo(0);
        assertThat(pageResponse.getPage().getTotalElements()).isEqualTo(100);
        assertThat(pageResponse.getPage().getTotalPages()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("전체 워크플로우가 올바르게 작동한다")
    void completeWorkflowWorks() {
        // 1. 컨트롤러에서 데이터 반환 시뮬레이션
        List<String> controllerData = List.of("data1", "data2", "data3");
        PageResponse<String> pageResponse = PageResponse.of(controllerData, 20, 0, 50L, 3);
        
        // Mock MethodParameter for PageResponse return type
        MethodParameter returnType = mock(MethodParameter.class);
        doReturn(PageResponse.class).when(returnType).getParameterType();
        doReturn(TestController.class).when(returnType).getDeclaringClass();
        
        // Mock ServerHttpResponse with HttpHeaders
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        doReturn(headers).when(response).getHeaders();
        
        // 2. ResponseAdvice가 PageResponse를 ApiResponse로 래핑
        Object wrappedResponse = responseAdvice.beforeBodyWrite(
            pageResponse,
            returnType,
            MediaType.APPLICATION_JSON,
            null,
            mock(ServerHttpRequest.class),
            response
        );
        
        // 3. 결과 검증
        assertThat(wrappedResponse).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<PageResponse<String>> apiResponse = (ApiResponse<PageResponse<String>>) wrappedResponse;
        
        assertThat(apiResponse.isSuccess()).isTrue();
        assertThat(apiResponse.getData()).isNotNull();
        assertThat(apiResponse.getData().getContent()).hasSize(3);
        assertThat(apiResponse.getData().getPage().getTotalElements()).isEqualTo(50);
        assertThat(apiResponse.getTimestamp()).isNotNull();
        assertThat(apiResponse.getRequestId()).isNotNull();
    }
}