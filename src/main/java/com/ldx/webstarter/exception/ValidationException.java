package com.ldx.webstarter.exception;

/**
 * 유효성 검증 예외 클래스.
 * 
 * <p>입력 데이터의 유효성 검증 실패를 나타내는 예외입니다.
 * HTTP 400 Bad Request 상태로 처리됩니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
public class ValidationException extends WebStarterException {
    
    /**
     * 기본 생성자.
     * 
     * @param message 에러 메시지
     */
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
    
    /**
     * 코드를 포함한 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    public ValidationException(String code, String message) {
        super(code, message);
    }
    
    /**
     * 원인 예외를 포함하는 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public ValidationException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}