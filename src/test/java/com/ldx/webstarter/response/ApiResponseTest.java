package com.ldx.webstarter.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResponse 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class ApiResponseTest {
    
    @Test
    @DisplayName("성공 응답을 생성한다")
    void createSuccessResponse() {
        String testData = "test data";
        ApiResponse<String> response = ApiResponse.success(testData);
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getRequestId()).isNotNull();
    }
    
    @Test
    @DisplayName("데이터 없는 성공 응답을 생성한다")
    void createSuccessResponseWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getRequestId()).isNotNull();
    }
    
    @Test
    @DisplayName("실패 응답을 생성한다")
    void createErrorResponse() {
        ErrorResponse error = ErrorResponse.of("TEST_ERROR", "Test error message");
        ApiResponse<Void> response = ApiResponse.error(error);
        
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getRequestId()).isNotNull();
    }
}