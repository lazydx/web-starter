package com.ldx.webstarter.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErrorResponse 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class ErrorResponseTest {
    
    @Test
    @DisplayName("기본 에러 응답을 생성한다")
    void createBasicErrorResponse() {
        String code = "TEST_ERROR";
        String message = "Test error message";
        
        ErrorResponse response = ErrorResponse.of(code, message);
        
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getDetails()).isNull();
    }
    
    @Test
    @DisplayName("상세 정보가 포함된 에러 응답을 생성한다")
    void createErrorResponseWithDetails() {
        String code = "VALIDATION_ERROR";
        String message = "Validation failed";
        List<String> details = List.of("field1: cannot be null", "field2: must be positive");
        
        ErrorResponse response = ErrorResponse.of(code, message, details);
        
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getDetails()).isEqualTo(details);
    }
}