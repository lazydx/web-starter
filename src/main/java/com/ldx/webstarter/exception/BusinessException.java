package com.ldx.webstarter.exception;

/**
 * 비즈니스 로직 예외 클래스.
 * 
 * <p>비즈니스 규칙 위반이나 도메인 로직 오류를 나타내는 예외입니다.
 * HTTP 400 Bad Request 상태로 처리됩니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
public class BusinessException extends WebStarterException {
    
    /**
     * 기본 생성자.
     * 
     * @param message 에러 메시지
     */
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message);
    }
    
    /**
     * 코드를 포함한 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    public BusinessException(String code, String message) {
        super(code, message);
    }
    
    /**
     * 원인 예외를 포함하는 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public BusinessException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}