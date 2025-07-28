package com.ldx.webstarter.exception;

import com.ldx.webstarter.infrastructure.exception.BusinessException;
import com.ldx.webstarter.infrastructure.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 예외 클래스들의 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class ExceptionTest {
    
    @Test
    @DisplayName("BusinessException을 생성한다")
    void createBusinessException() {
        String code = "BUSINESS_ERROR";
        String message = "Business logic error";
        
        BusinessException exception = new BusinessException(code, message);
        
        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
    
    @Test
    @DisplayName("원인이 있는 BusinessException을 생성한다")
    void createBusinessExceptionWithCause() {
        String code = "BUSINESS_ERROR";
        String message = "Business logic error";
        RuntimeException cause = new RuntimeException("Root cause");
        
        BusinessException exception = new BusinessException(code, message, cause);
        
        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
    
    @Test
    @DisplayName("NotFoundException을 생성한다")
    void createNotFoundException() {
        String code = "NOT_FOUND";
        String message = "Resource not found";
        
        NotFoundException exception = new NotFoundException(code, message);
        
        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
    
    @Test
    @DisplayName("원인이 있는 NotFoundException을 생성한다")
    void createNotFoundExceptionWithCause() {
        String code = "NOT_FOUND";
        String message = "Resource not found";
        RuntimeException cause = new RuntimeException("Root cause");
        
        NotFoundException exception = new NotFoundException(code, message, cause);
        
        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}