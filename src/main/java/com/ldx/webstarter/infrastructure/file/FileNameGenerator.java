package com.ldx.webstarter.infrastructure.file;

/**
 * 파일명 생성 인터페이스.
 *
 * <p>다양한 파일명 생성 전략을 지원하는 인터페이스입니다.
 * 전략 패턴을 활용하여 확장 가능한 구조로 설계되었습니다.
 *
 * @author web-starter
 * @since 1.1.0
 */
public interface FileNameGenerator {

    /**
     * 원본 파일명을 기반으로 고유한 파일명을 생성합니다.
     *
     * @param originalFilename 원본 파일명 (null이거나 빈 문자열 가능)
     * @return 생성된 고유 파일명
     * @throws FileNamingException 파일명 생성에 실패한 경우
     */
    String generateUniqueFilename(String originalFilename);

    /**
     * 원본 파일명을 기반으로 고유한 파일명을 생성합니다 (디렉토리 지원).
     *
     * @param originalFilename 원본 파일명 (null이거나 빈 문자열 가능)
     * @param directory 파일이 저장될 디렉토리 경로 (선택사항)
     * @return 생성된 고유 파일명
     * @throws FileNamingException 파일명 생성에 실패한 경우
     */
    String generateUniqueFilename(String originalFilename, String directory);

    /**
     * 파일명에서 확장자를 추출합니다.
     *
     * @param filename 파일명
     * @return 확장자 (없으면 빈 문자열)
     */
    String extractFileExtension(String filename);

    /**
     * 파일명이 유효한지 검증합니다.
     *
     * @param filename 검증할 파일명
     * @return 유효하면 true, 그렇지 않으면 false
     */
    boolean isValidFilename(String filename);

    /**
     * 파일명에서 허용되지 않는 문자를 제거하고 안전한 파일명으로 변환합니다.
     *
     * @param filename 원본 파일명
     * @return 안전하게 변환된 파일명
     */
    String sanitizeFilename(String filename);

    /**
     * 파일명 생성 예외.
     */
    class FileNamingException extends RuntimeException {
        
        private final String code;

        public FileNamingException(String code, String message) {
            super(message);
            this.code = code;
        }

        public FileNamingException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}