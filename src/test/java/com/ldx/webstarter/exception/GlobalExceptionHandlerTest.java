package com.ldx.webstarter.exception;

import com.ldx.webstarter.infrastructure.exception.BusinessException;
import com.ldx.webstarter.infrastructure.exception.GlobalExceptionHandler;
import com.ldx.webstarter.infrastructure.exception.NotFoundException;
import com.ldx.webstarter.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GlobalExceptionHandler 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    @Test
    @DisplayName("BusinessException을 400으로 처리한다")
    void handleBusinessExceptionReturns400() {
        BusinessException exception = new BusinessException("BUSINESS_ERROR", "Business error occurred");
        
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().getCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(response.getBody().getError().getMessage()).isEqualTo("Business error occurred");
    }
    
    @Test
    @DisplayName("NotFoundException을 404로 처리한다")
    void handleNotFoundExceptionReturns404() {
        NotFoundException exception = new NotFoundException("NOT_FOUND", "Resource not found");
        
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotFoundException(exception);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    @Test
    @DisplayName("MethodArgumentNotValidException을 422로 처리한다")
    void handleValidationExceptionReturns422() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("testObject", "testField", "Test error message");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(exception.getMessage()).thenReturn("Validation failed");
        
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getError().getDetails()).hasSize(1);
        assertThat(response.getBody().getError().getDetails().get(0)).contains("testField");
    }
    
    @Test
    @DisplayName("일반 Exception을 500으로 처리한다")
    void handleGeneralExceptionReturns500() {
        Exception exception = new RuntimeException("Unexpected error");
        
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getError().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }
}