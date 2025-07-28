package com.ldx.webstarter.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 표준 에러 응답 클래스.
 * 
 * <p>API 에러 발생 시 일관된 형식의 에러 정보를 제공합니다.
 * 에러 코드, 메시지, 상세 정보를 포함합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final String code;
    private final String message;
    private final List<String> details;
    
    private ErrorResponse(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
    
    /**
     * 기본 에러 응답을 생성합니다.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }
    
    /**
     * 상세 정보가 포함된 에러 응답을 생성합니다.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param details 상세 에러 정보 목록
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(String code, String message, List<String> details) {
        return new ErrorResponse(code, message, details);
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<String> getDetails() {
        return details;
    }
}