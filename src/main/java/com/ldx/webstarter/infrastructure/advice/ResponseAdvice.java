package com.ldx.webstarter.infrastructure.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.webstarter.infrastructure.properties.ResponseProperties;
import com.ldx.webstarter.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 응답 표준화 어드바이스.
 * 
 * <p>모든 컨트롤러의 응답을 ApiResponse 형식으로 자동 래핑합니다.
 * 이미 ApiResponse 형식이면 그대로 반환하고, 그렇지 않으면 성공 응답으로 래핑합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseAdvice.class);
    
    private final ObjectMapper objectMapper;
    
    public ResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 응답 래핑 대상인지 확인합니다.
     * 
     * @param returnType 반환 타입
     * @param converterType 메시지 컨버터 타입
     * @return 래핑 대상이면 true
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        logger.debug("ResponseAdvice.supports() called - returnType: {}, converterType: {}", 
                    returnType != null ? returnType.getParameterType().getSimpleName() : "null", 
                    converterType != null ? converterType.getSimpleName() : "null");
        
        // 필수 파라미터가 null이면 건너뜀
        if (returnType == null) {
            logger.debug("Skipping - returnType is null");
            return false;
        }
        
        // 이미 ApiResponse 타입이면 건너뜀
        if (returnType.getParameterType().equals(ApiResponse.class)) {
            logger.debug("Skipping - already ApiResponse type");
            return false;
        }
        
        // Resource 타입(파일 다운로드)은 건너뜀
        if (Resource.class.isAssignableFrom(returnType.getParameterType())) {
            logger.debug("Skipping - Resource type (file download)");
            return false;
        }
        
        // ResponseEntity<Resource> 타입도 건너뜀
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            // ResponseEntity의 Generic 타입이 Resource인지 확인
            if (returnType.getGenericParameterType().getTypeName().contains("Resource")) {
                logger.debug("Skipping - ResponseEntity<Resource> type (file download)");
                return false;
            }
        }
        
        // Actuator 엔드포인트는 건너뜀
        String declaringClassName = returnType.getDeclaringClass().getName();
        if (declaringClassName.startsWith("org.springframework.boot.actuator") ||
            declaringClassName.contains("HealthEndpoint") ||
            declaringClassName.contains("InfoEndpoint")) {
            logger.debug("Skipping - actuator endpoint: {}", declaringClassName);
            return false;
        }
        
        // String 타입의 경우 특별 처리 (JSON으로 변환하여 래핑)
        if (converterType != null && converterType.equals(StringHttpMessageConverter.class)) {
            logger.debug("String response detected - will wrap with ApiResponse");
            return true;
        }
        
        // void 타입은 반드시 처리 (빈 성공 응답 반환)
        if (returnType.getParameterType().equals(Void.TYPE)) {
            logger.debug("Supporting void method - will return empty success response");
            return true;
        }
        
        logger.debug("ResponseAdvice will process this response");
        return true;
    }
    
    /**
     * 응답 본문을 래핑합니다.
     * 
     * @param body 원본 응답 본문
     * @param returnType 반환 타입
     * @param selectedContentType 선택된 컨텐츠 타입
     * @param selectedConverterType 선택된 컨버터 타입
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 래핑된 응답
     */
    @Override
    public Object beforeBodyWrite(
            Object body, 
            MethodParameter returnType, 
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, 
            ServerHttpRequest request,
            ServerHttpResponse response) {
        
        logger.debug("ResponseAdvice.beforeBodyWrite() called - body: {}, returnType: {}, selectedConverterType: {}", 
                    body != null ? body.getClass().getSimpleName() : "null", 
                    returnType != null ? returnType.getParameterType().getSimpleName() : "null",
                    selectedConverterType != null ? selectedConverterType.getSimpleName() : "null");
        
        // 이미 ApiResponse이면 그대로 반환
        if (body instanceof ApiResponse) {
            logger.debug("Body is already ApiResponse, returning as-is");
            return body;
        }
        
        // void 타입이면 빈 성공 응답 반환
        if (body == null && returnType != null && returnType.getParameterType().equals(Void.TYPE)) {
            logger.debug("Void response, returning empty success response");
            return ApiResponse.success();
        }
        
        // String 타입의 경우 JSON으로 직렬화하기 위해 특별 처리
        if (selectedConverterType != null && selectedConverterType.equals(StringHttpMessageConverter.class)) {
            logger.debug("Handling String response - converting to JSON");
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            ApiResponse<?> wrappedResponse = ApiResponse.success(body);
            
            try {
                // ObjectMapper를 사용하여 JSON 문자열로 변환
                String jsonResponse = objectMapper.writeValueAsString(wrappedResponse);
                logger.debug("Converted String response to JSON: {}", jsonResponse);
                return jsonResponse;
            } catch (Exception e) {
                logger.error("Failed to convert response to JSON", e);
                return wrappedResponse;
            }
        }
        
        // Content-Type을 JSON으로 설정
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 일반 응답을 성공 응답으로 래핑
        ApiResponse<?> wrappedResponse = ApiResponse.success(body);
        logger.debug("Wrapping response with ApiResponse: {}", wrappedResponse);
        return wrappedResponse;
    }
}