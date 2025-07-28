package com.ldx.webstarter.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.webstarter.infrastructure.advice.ResponseAdvice;
import com.ldx.webstarter.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * ResponseAdvice 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class ResponseAdviceTest {
    
    private ResponseAdvice responseAdvice;
    
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        responseAdvice = new ResponseAdvice(objectMapper);
    }
    
    @Test
    @DisplayName("일반 응답을 ApiResponse로 래핑한다")
    void wrapsRegularResponseInApiResponse() {
        String originalBody = "Hello World";
        
        // Mock MethodParameter for String return type
        MethodParameter returnType = mock(MethodParameter.class);
        doReturn(String.class).when(returnType).getParameterType();
        doReturn(TestController.class).when(returnType).getDeclaringClass();
        
        // Mock ServerHttpResponse with HttpHeaders
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        doReturn(headers).when(response).getHeaders();
        
        Object result = responseAdvice.beforeBodyWrite(
            originalBody,
            returnType,
            MediaType.APPLICATION_JSON,
            null, // converterType
            mock(ServerHttpRequest.class),
            response
        );
        
        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<String> apiResponse = (ApiResponse<String>) result;
        assertThat(apiResponse.isSuccess()).isTrue();
        assertThat(apiResponse.getData()).isEqualTo(originalBody);
    }
    
    // Test controller class for mocking
    static class TestController {}
    
    @Test
    @DisplayName("ApiResponse는 그대로 반환한다")
    void returnsApiResponseAsIs() {
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
            null, // converterType
            mock(ServerHttpRequest.class),
            response
        );
        
        assertThat(result).isSameAs(originalResponse);
    }
    
    @Test
    @DisplayName("null 응답을 빈 성공 응답으로 래핑한다")
    void wrapsNullResponseInEmptySuccessResponse() {
        // Void.TYPE을 반환하는 메서드 파라미터 모킹
        MethodParameter returnType = mock(MethodParameter.class);
        doReturn(Void.TYPE).when(returnType).getParameterType();
        doReturn(TestController.class).when(returnType).getDeclaringClass();
        
        // Mock ServerHttpResponse with HttpHeaders
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        doReturn(headers).when(response).getHeaders();
        
        Object result = responseAdvice.beforeBodyWrite(
            null,
            returnType,
            MediaType.APPLICATION_JSON,
            null, // converterType
            mock(ServerHttpRequest.class),
            response
        );
        
        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Void> apiResponse = (ApiResponse<Void>) result;
        assertThat(apiResponse.isSuccess()).isTrue();
    }
}