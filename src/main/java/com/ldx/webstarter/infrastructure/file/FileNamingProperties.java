package com.ldx.webstarter.infrastructure.file;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;
import java.util.List;

/**
 * 파일 명명 규칙 설정 Properties.
 *
 * <p>파일명 생성 시 사용되는 모든 설정값들을 관리합니다.
 * 매직넘버와 하드코딩된 값들을 외부 설정으로 관리하여
 * 다양한 환경과 요구사항에 유연하게 대응할 수 있습니다.
 *
 * @author web-starter
 * @since 1.1.0
 */
@ConfigurationProperties(prefix = "web-starter.file-naming")
@Validated
public class FileNamingProperties {

    /**
     * 파일명 생성 전략.
     */
    @NotNull
    private NamingStrategy strategy = NamingStrategy.TIMESTAMP_UUID;

    /**
     * 타임스탬프 패턴.
     * 기본값: "yyyyMMdd_HHmmss"
     */
    @NotBlank
    private String timestampPattern = "yyyyMMdd_HHmmss";

    /**
     * UUID 길이 (1-36 범위).
     * 기본값: 8
     */
    @Min(1)
    @Max(36)
    private int uuidLength = 8;

    /**
     * 구분자 문자열.
     * 기본값: "_"
     */
    @NotBlank
    private String separator = "_";

    /**
     * 파일명 생성 시 사용할 시간대.
     * 기본값: 시스템 기본 시간대
     */
    @NotNull
    private ZoneId timeZone = ZoneId.systemDefault();

    /**
     * 파일명 최대 길이 제한.
     * 기본값: 255 (대부분 파일시스템 제한)
     */
    @Min(50)
    @Max(500)
    private int maxFilenameLength = 255;

    /**
     * 충돌 방지를 위한 재시도 횟수.
     * 기본값: 3
     */
    @Min(1)
    @Max(10)
    private int maxRetryAttempts = 3;

    /**
     * 원본 파일명 자르기 길이.
     * ORIGINAL_WITH_UUID 전략에서 원본 파일명 부분이 너무 길 때 이 길이로 자릅니다.
     * 기본값: 50
     */
    @Min(10)
    @Max(200)
    private int originalFileNameTruncateLength = 50;

    /**
     * 파일명에 허용되지 않는 문자들.
     * Windows/Unix 호환성을 위한 기본 제한 문자.
     */
    @NotNull
    private List<String> forbiddenCharacters = List.of(
            "<", ">", ":", "\"", "|", "?", "*", "/", "\\", "\0"
    );

    /**
     * 예약된 파일명 목록 (Windows 호환성).
     */
    @NotNull
    private List<String> reservedNames = List.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );

    // === 접근자 메서드들 ===

    public NamingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(NamingStrategy strategy) {
        this.strategy = strategy;
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }

    public void setTimestampPattern(String timestampPattern) {
        this.timestampPattern = timestampPattern;
    }

    public int getUuidLength() {
        return uuidLength;
    }

    public void setUuidLength(int uuidLength) {
        this.uuidLength = uuidLength;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    public int getMaxFilenameLength() {
        return maxFilenameLength;
    }

    public void setMaxFilenameLength(int maxFilenameLength) {
        this.maxFilenameLength = maxFilenameLength;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public int getOriginalFileNameTruncateLength() {
        return originalFileNameTruncateLength;
    }

    public void setOriginalFileNameTruncateLength(int originalFileNameTruncateLength) {
        this.originalFileNameTruncateLength = originalFileNameTruncateLength;
    }

    public List<String> getForbiddenCharacters() {
        return forbiddenCharacters;
    }

    public void setForbiddenCharacters(List<String> forbiddenCharacters) {
        this.forbiddenCharacters = forbiddenCharacters;
    }

    public List<String> getReservedNames() {
        return reservedNames;
    }

    public void setReservedNames(List<String> reservedNames) {
        this.reservedNames = reservedNames;
    }

    /**
     * 파일명 생성 전략 열거형.
     */
    public enum NamingStrategy {
        /**
         * 타임스탬프 + UUID 조합 (기본).
         * 예: 20231109_143022_a1b2c3d4.jpg
         */
        TIMESTAMP_UUID,

        /**
         * UUID만 사용.
         * 예: a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
         */
        UUID_ONLY,

        /**
         * 타임스탬프 + 순번.
         * 예: 20231109_143022_001.jpg
         */
        TIMESTAMP_SEQUENCE,

        /**
         * 원본 파일명 보존 + UUID 접미사.
         * 예: originalfile_a1b2c3d4.jpg
         */
        ORIGINAL_WITH_UUID
    }
}