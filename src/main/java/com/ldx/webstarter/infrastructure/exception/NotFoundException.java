package com.ldx.webstarter.infrastructure.exception;

/**
 * 리소스를 찾을 수 없음을 나타내는 예외 클래스.
 * 
 * <p>요청한 리소스가 존재하지 않을 때 발생하는 예외입니다.
 * HTTP 404 Not Found 상태로 처리됩니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
public class NotFoundException extends RuntimeException {
    
    private final String code;
    
    /**
     * 기본 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 원인 예외를 포함하는 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public NotFoundException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}