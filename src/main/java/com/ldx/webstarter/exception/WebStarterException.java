package com.ldx.webstarter.exception;

/**
 * Web Starter의 기본 예외 클래스.
 * 
 * <p>Web Starter 라이브러리에서 발생하는 모든 예외의 기본 클래스입니다.
 * 사용자가 쉽게 접근할 수 있도록 최상위 패키지에 위치합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
public class WebStarterException extends RuntimeException {
    
    private final String code;
    
    /**
     * 기본 생성자.
     * 
     * @param message 에러 메시지
     */
    public WebStarterException(String message) {
        super(message);
        this.code = "WEBSTARTER_ERROR";
    }
    
    /**
     * 코드를 포함한 생성자.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    public WebStarterException(String code, String message) {
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
    public WebStarterException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * 에러 코드를 반환합니다.
     * 
     * @return 에러 코드
     */
    public String getCode() {
        return code;
    }
}