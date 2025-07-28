package com.ldx.webstarter.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 표준 API 응답 래퍼 클래스.
 * 
 * <p>모든 API 응답을 통일된 형식으로 제공합니다.
 * success 필드를 통해 성공/실패를 구분하고, 타임스탬프와 요청 ID를 포함합니다.
 * 
 * @param <T> 응답 데이터의 타입
 * @author web-starter
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final ErrorResponse error;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private final LocalDateTime timestamp;
    private final String requestId;
    
    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.requestId = UUID.randomUUID().toString();
    }
    
    /**
     * 성공 응답을 생성합니다.
     * 
     * @param <T> 데이터 타입
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    /**
     * 데이터 없는 성공 응답을 생성합니다.
     * 
     * @return 성공 응답 객체
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }
    
    /**
     * 실패 응답을 생성합니다.
     * 
     * @param error 에러 정보
     * @return 실패 응답 객체
     */
    public static ApiResponse<Void> error(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public T getData() {
        return data;
    }
    
    public ErrorResponse getError() {
        return error;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getRequestId() {
        return requestId;
    }
}