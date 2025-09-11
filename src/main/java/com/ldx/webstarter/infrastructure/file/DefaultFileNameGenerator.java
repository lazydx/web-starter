package com.ldx.webstarter.infrastructure.file;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 기본 파일명 생성기 구현체.
 *
 * <p>다양한 네이밍 전략을 지원하며, 설정 기반으로 동작하는
 * 파일명 생성기의 기본 구현체입니다.
 *
 * @author web-starter
 * @since 1.1.0
 */
public class DefaultFileNameGenerator implements FileNameGenerator {

    private final FileNamingProperties properties;
    private final Pattern forbiddenCharPattern;
    private final ThreadLocalRandom random;

    public DefaultFileNameGenerator(FileNamingProperties properties) {
        this.properties = properties;
        this.random = ThreadLocalRandom.current();
        
        // 금지된 문자들을 정규식 패턴으로 컴파일 (성능 최적화)
        StringBuilder patternBuilder = new StringBuilder("[");
        for (String forbidden : properties.getForbiddenCharacters()) {
            patternBuilder.append(Pattern.quote(forbidden));
        }
        patternBuilder.append("]");
        this.forbiddenCharPattern = Pattern.compile(patternBuilder.toString());
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        return generateUniqueFilename(originalFilename, null);
    }

    @Override
    public String generateUniqueFilename(String originalFilename, String directory) {
        String cleanOriginal = sanitizeFilename(originalFilename);
        String extension = extractFileExtension(cleanOriginal);
        
        for (int attempt = 1; attempt <= properties.getMaxRetryAttempts(); attempt++) {
            try {
                String generatedName = generateNameByStrategy(cleanOriginal, extension, attempt);
                
                // 길이 제한 검사
                if (generatedName.length() > properties.getMaxFilenameLength()) {
                    generatedName = truncateFilename(generatedName, extension);
                }
                
                // 최종 검증
                if (isValidFilename(generatedName)) {
                    return generatedName;
                }
                
            } catch (Exception e) {
                if (attempt == properties.getMaxRetryAttempts()) {
                    throw new FileNamingException("FILE_NAME_GENERATION_FAILED", 
                        "Failed to generate filename after " + attempt + " attempts", e);
                }
            }
        }
        
        throw new FileNamingException("FILE_NAME_GENERATION_EXHAUSTED", 
            "Exhausted all " + properties.getMaxRetryAttempts() + " attempts to generate unique filename");
    }

    @Override
    public String extractFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public boolean isValidFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }

        // 길이 검사
        if (filename.length() > properties.getMaxFilenameLength()) {
            return false;
        }

        // 예약된 이름 검사 (확장자 제외하고)
        String nameWithoutExtension = filename.contains(".") 
            ? filename.substring(0, filename.lastIndexOf('.'))
            : filename;
        
        if (properties.getReservedNames().contains(nameWithoutExtension.toUpperCase())) {
            return false;
        }

        // 금지된 문자 검사
        if (forbiddenCharPattern.matcher(filename).find()) {
            return false;
        }

        // 시작/끝 공백이나 점 검사
        if (filename.startsWith(" ") || filename.endsWith(" ") || 
            filename.endsWith(".") || filename.startsWith(".")) {
            return false;
        }

        return true;
    }

    @Override
    public String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "unnamed";
        }

        // 금지된 문자 제거
        String sanitized = forbiddenCharPattern.matcher(filename).replaceAll("_");
        
        // 연속된 공백을 하나로 변경
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        // 시작/끝 공백 제거
        sanitized = sanitized.trim();
        
        // 예약된 이름 회피
        String nameWithoutExtension = sanitized.contains(".") 
            ? sanitized.substring(0, sanitized.lastIndexOf('.'))
            : sanitized;
        
        if (properties.getReservedNames().contains(nameWithoutExtension.toUpperCase())) {
            String extension = extractFileExtension(sanitized);
            sanitized = nameWithoutExtension + "_file" + 
                       (StringUtils.hasText(extension) ? "." + extension : "");
        }

        return StringUtils.hasText(sanitized) ? sanitized : "unnamed";
    }

    /**
     * 네이밍 전략에 따라 파일명을 생성합니다.
     */
    private String generateNameByStrategy(String originalFilename, String extension, int attempt) {
        return switch (properties.getStrategy()) {
            case TIMESTAMP_UUID -> generateTimestampUuidName(extension);
            case UUID_ONLY -> generateUuidOnlyName(extension);
            case TIMESTAMP_SEQUENCE -> generateTimestampSequenceName(extension, attempt);
            case ORIGINAL_WITH_UUID -> generateOriginalWithUuidName(originalFilename, extension);
        };
    }

    /**
     * 타임스탬프 + UUID 조합 파일명 생성.
     */
    private String generateTimestampUuidName(String extension) {
        String timestamp = ZonedDateTime.now(properties.getTimeZone())
            .format(DateTimeFormatter.ofPattern(properties.getTimestampPattern()));
        
        String uuid = UUID.randomUUID().toString().substring(0, properties.getUuidLength());
        
        StringBuilder nameBuilder = new StringBuilder()
            .append(timestamp)
            .append(properties.getSeparator())
            .append(uuid);
        
        if (StringUtils.hasText(extension)) {
            nameBuilder.append(".").append(extension);
        }
        
        return nameBuilder.toString();
    }

    /**
     * UUID만 사용하는 파일명 생성.
     */
    private String generateUuidOnlyName(String extension) {
        String uuid = properties.getUuidLength() >= 36 
            ? UUID.randomUUID().toString()
            : UUID.randomUUID().toString().substring(0, properties.getUuidLength());
        
        return StringUtils.hasText(extension) ? uuid + "." + extension : uuid;
    }

    /**
     * 타임스탬프 + 순번 파일명 생성.
     */
    private String generateTimestampSequenceName(String extension, int sequence) {
        String timestamp = ZonedDateTime.now(properties.getTimeZone())
            .format(DateTimeFormatter.ofPattern(properties.getTimestampPattern()));
        
        // 3자리 시퀀스 번호
        String sequenceStr = String.format("%03d", sequence);
        
        StringBuilder nameBuilder = new StringBuilder()
            .append(timestamp)
            .append(properties.getSeparator())
            .append(sequenceStr);
        
        if (StringUtils.hasText(extension)) {
            nameBuilder.append(".").append(extension);
        }
        
        return nameBuilder.toString();
    }

    /**
     * 원본 파일명 + UUID 접미사 파일명 생성.
     */
    private String generateOriginalWithUuidName(String originalFilename, String extension) {
        String baseName = StringUtils.hasText(originalFilename) 
            ? removeExtension(originalFilename) 
            : "file";
        
        // 원본 파일명이 너무 길면 잘라내기
        int truncateLength = properties.getOriginalFileNameTruncateLength();
        if (baseName.length() > truncateLength) {
            baseName = baseName.substring(0, truncateLength);
        }
        
        String uuid = UUID.randomUUID().toString().substring(0, properties.getUuidLength());
        
        StringBuilder nameBuilder = new StringBuilder()
            .append(baseName)
            .append(properties.getSeparator())
            .append(uuid);
        
        if (StringUtils.hasText(extension)) {
            nameBuilder.append(".").append(extension);
        }
        
        return nameBuilder.toString();
    }

    /**
     * 파일명에서 확장자를 제거합니다.
     */
    private String removeExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }

    /**
     * 파일명이 길이 제한을 초과할 경우 잘라냅니다.
     */
    private String truncateFilename(String filename, String extension) {
        int maxLength = properties.getMaxFilenameLength();
        int extensionLength = StringUtils.hasText(extension) ? extension.length() + 1 : 0; // +1 for dot
        int allowedNameLength = maxLength - extensionLength;
        
        if (allowedNameLength <= 0) {
            throw new FileNamingException("FILE_NAME_TOO_LONG", 
                "Extension length exceeds maximum filename length");
        }
        
        String nameWithoutExtension = StringUtils.hasText(extension) 
            ? filename.substring(0, filename.lastIndexOf('.'))
            : filename;
        
        String truncatedName = nameWithoutExtension.length() > allowedNameLength 
            ? nameWithoutExtension.substring(0, allowedNameLength)
            : nameWithoutExtension;
        
        return StringUtils.hasText(extension) 
            ? truncatedName + "." + extension 
            : truncatedName;
    }
}