package com.ldx.webstarter.infrastructure.exception;

import com.ldx.webstarter.response.ApiResponse;
import com.ldx.webstarter.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기.
 * 
 * <p>애플리케이션에서 발생하는 모든 예외를 일관된 형식으로 처리합니다.
 * 예외 유형에 따라 적절한 HTTP 상태 코드와 에러 메시지를 반환합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 비즈니스 예외를 처리합니다.
     * 
     * @param e BusinessException
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.warn("Business exception occurred: {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of(e.getCode(), e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 검증 예외를 처리합니다.
     * 
     * @param e MethodArgumentNotValidException
     * @return 422 Unprocessable Entity 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation exception occurred: {}", e.getMessage());
        
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", "입력값 검증에 실패했습니다.", details);
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.unprocessableEntity().body(response);
    }
    
    /**
     * 리소스를 찾을 수 없는 예외를 처리합니다.
     * 
     * @param e NotFoundException
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException e) {
        logger.warn("Resource not found: {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of(e.getCode(), e.getMessage());
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 지원하지 않는 HTTP 메서드 예외를 처리합니다.
     * 
     * @param e HttpRequestMethodNotSupportedException
     * @return 405 Method Not Allowed 응답
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("Method not supported: {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of("METHOD_NOT_ALLOWED", 
            String.format("지원하지 않는 HTTP 메서드입니다. 지원되는 메서드: %s", String.join(", ", e.getSupportedMethods())));
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * 핸들러를 찾을 수 없는 예외를 처리합니다.
     * 
     * @param e NoHandlerFoundException
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("No handler found: {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of("NOT_FOUND", 
            String.format("요청한 리소스를 찾을 수 없습니다: %s %s", e.getHttpMethod(), e.getRequestURL()));
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 정적 리소스를 찾을 수 없는 예외를 처리합니다.
     * 
     * @param e NoResourceFoundException
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        logger.warn("No resource found: {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of("NOT_FOUND", 
            String.format("요청한 리소스를 찾을 수 없습니다: %s", e.getResourcePath()));
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 일반적인 예외를 처리합니다.
     * 
     * @param e Exception
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        logger.error("Unexpected exception occurred", e);
        
        ErrorResponse error = ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        ApiResponse<Void> response = ApiResponse.error(error);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 필드 에러를 포맷팅합니다.
     * 
     * @param fieldError 필드 에러
     * @return 포맷된 에러 메시지
     */
    private String formatFieldError(FieldError fieldError) {
        return String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage());
    }
}