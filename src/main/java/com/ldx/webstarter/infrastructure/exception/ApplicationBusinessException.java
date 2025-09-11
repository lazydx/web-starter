package com.ldx.webstarter.infrastructure.exception;

/**
 * Application Layer 비즈니스 예외.
 * 
 * <p>헥사고날 아키텍처에서 Application Layer에서 발생하는 비즈니스 예외를 나타냅니다.
 * Domain Layer의 예외와 구분하여 아키텍처 경계를 명확히 합니다.
 * 
 * <p>사용 시나리오:
 * - Application Service에서 비즈니스 규칙 위반 시
 * - Use Case 실행 중 비즈니스 로직 오류 발생 시
 * - Domain 예외를 Application 예외로 변환할 때
 * 
 * <p>헥사고날 모드에서만 HexagonalGlobalExceptionHandler가 처리하며,
 * Traditional 모드에서는 기존 GlobalExceptionHandler가 처리합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
public class ApplicationBusinessException extends RuntimeException {
    
    /**
     * 예외 코드.
     */
    private final String code;
    
    /**
     * 기본 생성자.
     *
     * @param message 예외 메시지
     */
    public ApplicationBusinessException(String message) {
        this("APPLICATION_ERROR", message);
    }
    
    /**
     * 코드와 메시지를 지정하는 생성자.
     *
     * @param code 예외 코드
     * @param message 예외 메시지
     */
    public ApplicationBusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 원인과 함께 예외를 생성하는 생성자.
     *
     * @param code 예외 코드
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public ApplicationBusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * Domain 예외를 Application 예외로 변환하는 팩토리 메서드.
     * 헥사고날 아키텍처에서 Domain Layer의 예외를 Application Layer로 변환할 때 사용합니다.
     *
     * @param domainException Domain Layer에서 발생한 예외
     * @return Application Layer 예외
     */
    public static ApplicationBusinessException fromDomainException(Exception domainException) {
        return new ApplicationBusinessException(
            "DOMAIN_ERROR", 
            "Domain layer error: " + domainException.getMessage(), 
            domainException
        );
    }
    
    /**
     * 예외 코드를 반환합니다.
     *
     * @return 예외 코드
     */
    public String getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return "ApplicationBusinessException{" +
                "code='" + code + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}