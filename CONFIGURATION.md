# Web Starter 설정 가이드 - 실제 구현 분석

## 개요

이 문서는 web-starter 프로젝트의 실제 구현된 모든 Configuration Properties를 코드 분석을 통해 정리한 완전한 설정 가이드입니다.

## 🏗️ Properties 구조 개요

### 루트 Properties
- **`WebStarterProperties`**: `web-starter` prefix - 메인 설정 클래스
- **모든 하위 Properties 통합 관리**
- **FeatureToggle 기반 기능 제어**
- **Mode 기반 아키텍처 지원**

## 📋 전체 설정 구조도

```yaml
web-starter:                          # WebStarterProperties
  enabled: true                       # 전체 스타터 활성화
  mode: TRADITIONAL                   # 아키텍처 모드

  # FeatureToggle 구조
  response-toggle:                    # API 응답 표준화 토글
    enabled: true
  exception-toggle:                   # 전역 예외 처리 토글
    enabled: true
  cors-toggle:                        # CORS 설정 토글
    enabled: true
  file-toggle:                        # 파일 처리 토글
    enabled: false                    # 기본 비활성화
  debug-toggle:                       # 디버그 기능 토글
    enabled: false                    # 기본 비활성화

  # 중첩된 Properties
  response: {...}                     # ResponseProperties
  cors: {...}                         # CorsProperties
  pagination: {...}                   # PaginationProperties
  file-storage: {...}                 # FileStorageProperties
  file-naming: {...}                  # FileNamingProperties
  debug: {...}                        # DebugProperties
```

## 1️⃣ WebStarterProperties (`web-starter`)

### 기본 설정
```yaml
web-starter:
  enabled: true                       # Boolean - 전체 스타터 활성화 (기본값: true)
  mode: TRADITIONAL                   # Mode enum - 아키텍처 모드
```

### Mode 열거형
```java
public enum Mode {
    TRADITIONAL,    // 전통적 MVC 아키텍처 - 모든 레이어에서 web-starter 자유 사용
    HEXAGONAL      // 헥사고날 아키텍처 - Domain Layer에서 web-starter 사용 제한
}
```

### FeatureToggle 설정
```yaml
web-starter:
  response-toggle:
    enabled: true                     # API 응답 표준화 기능 (기본값: true)
  exception-toggle:
    enabled: true                     # 전역 예외 처리 기능 (기본값: true)
  cors-toggle:
    enabled: true                     # CORS 설정 기능 (기본값: true)
  file-toggle:
    enabled: false                    # 파일 처리 기능 (기본값: false)
  debug-toggle:
    enabled: false                    # 디버그 기능 (기본값: false)
```

### 편의 메서드
WebStarterProperties는 복합 조건 체크를 위한 편의 메서드들을 제공합니다:
- `isResponseEnabled()`: `enabled && response.enabled && responseToggle.enabled`
- `isExceptionEnabled()`: `enabled && exceptionToggle.enabled`
- `isCorsEnabled()`: `enabled && cors.enabled && corsToggle.enabled`
- `isFileEnabled()`: `enabled && fileToggle.enabled`
- `isDebugEnabled()`: `enabled && debugToggle.enabled`

## 2️⃣ ResponseProperties (`web-starter.response`)

```yaml
web-starter:
  response:
    enabled: true                     # Boolean - 응답 표준화 활성화 (기본값: true)
    wrap-response: true               # Boolean - 자동 응답 래핑 (기본값: true)
    include-request-id: true          # Boolean - 요청 ID 포함 (기본값: true)
    include-timestamp: true           # Boolean - 타임스탬프 포함 (기본값: true)
```

**기능**: API 응답을 `ApiResponse<T>` 형식으로 자동 래핑

## 3️⃣ CorsProperties (`web-starter.cors`)

```yaml
web-starter:
  cors:
    enabled: true                     # Boolean - CORS 설정 활성화 (기본값: true)
    allowed-origins:                  # List<String> - 허용 오리진 (기본값: ["*"])
      - "*"
    allowed-methods:                  # List<String> - 허용 HTTP 메서드
      - "GET"
      - "POST"
      - "PUT"
      - "DELETE"
      - "PATCH"
      - "OPTIONS"
    allowed-headers:                  # List<String> - 허용 헤더 (기본값: ["*"])
      - "*"
    allow-credentials: false          # Boolean - 자격증명 허용 (기본값: false)
    max-age: 30m                      # Duration - preflight 캐시 시간 (기본값: 30분)
    path-pattern: "/**"               # String - CORS 적용 경로 패턴 (기본값: "/**")
```

**검증**: Duration 타입의 max-age 설정 지원

## 4️⃣ PaginationProperties (`web-starter.pagination`)

```yaml
web-starter:
  pagination:
    enabled: true                     # Boolean - 페이지네이션 제한 활성화 (기본값: true)
    default-size: 20                  # int - 기본 페이지 크기 (@Min(1), @Max(100), 기본값: 20)
    max-size: 100                     # int - 최대 페이지 크기 (@Min(1), @Max(1000), 기본값: 100)
    max-elements: 5000                # long - 최대 총 요소 수 (@Min(1), 기본값: 5000L)
    page-parameter: "page"            # String - 페이지 파라미터 명 (기본값: "page")
    size-parameter: "size"            # String - 크기 파라미터 명 (기본값: "size")
    sort-parameter: "sort"            # String - 정렬 파라미터 명 (기본값: "sort")
```

**검증**: Jakarta Validation 어노테이션으로 값 범위 제한

## 5️⃣ FileStorageProperties (`web-starter.file-storage`)

### 기본 설정
```yaml
web-starter:
  file-storage:
    enabled: true                     # Boolean - 파일 저장소 활성화 (기본값: true)
```

### Upload 설정
```yaml
web-starter:
  file-storage:
    upload:
      max-file-size: "10MB"           # String - 최대 파일 크기 (기본값: "10MB")
      max-request-size: "100MB"       # String - 최대 요청 크기 (기본값: "100MB")
      allowed-extensions:             # List<String> - 허용 확장자
        - "jpg"
        - "jpeg"
        - "png"
        - "gif"
        - "pdf"
        - "doc"
        - "docx"
        - "txt"
      allowed-mime-types:             # List<String> - 허용 MIME 타입
        - "image/jpeg"
        - "image/png"
        - "image/gif"
        - "application/pdf"
        - "application/msword"
        - "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        - "text/plain"
      enable-virus-scanning: false    # Boolean - 바이러스 스캔 활성화 (기본값: false)
      temp-dir: "${java.io.tmpdir}/webstarter-uploads"  # String - 임시 디렉토리
      file-content-validation-header-size: 512  # int - 파일 내용 검증 헤더 크기
```

### Download 설정
```yaml
web-starter:
  file-storage:
    download:
      enable-range-requests: true     # Boolean - Range 요청 지원 (기본값: true)
      cache-max-age: 3600             # long - 캐시 최대 수명(초) (기본값: 3600)
      default-content-type: "application/octet-stream"  # String - 기본 Content-Type
```

### Local 저장소 설정
```yaml
web-starter:
  file-storage:
    local:
      base-path: "./uploads"          # String - 기본 저장 경로 (기본값: "./uploads")
      create-directories: true        # Boolean - 디렉토리 자동 생성 (기본값: true)
```

### Azure 저장소 설정
```yaml
web-starter:
  file-storage:
    azure:
      enabled: false                  # Boolean - Azure 저장소 활성화 (기본값: false)
      connection-string: ""           # String - Azure 연결 문자열
      container-name: ""              # String - 컨테이너 명
      path-prefix: "uploads/"         # String - 경로 접두사 (기본값: "uploads/")
```

### Format 설정
```yaml
web-starter:
  file-storage:
    format:
      bytes-per-unit: 1024            # int - 바이트 변환 단위 (기본값: 1024, 1000으로 설정시 SI 단위)
```

## 6️⃣ FileNamingProperties (`web-starter.file-naming`)

```yaml
web-starter:
  file-naming:
    strategy: TIMESTAMP_UUID          # NamingStrategy enum - 파일명 생성 전략
    timestamp-pattern: "yyyyMMdd_HHmmss"  # String - 타임스탬프 패턴 (@NotBlank)
    uuid-length: 8                    # int - UUID 길이 (@Min(1), @Max(36), 기본값: 8)
    separator: "_"                    # String - 구분자 (@NotBlank, 기본값: "_")
    time-zone: "system"               # ZoneId - 시간대 (기본값: 시스템 기본)
    max-filename-length: 255          # int - 파일명 최대 길이 (@Min(50), @Max(500))
    max-retry-attempts: 3             # int - 충돌 방지 재시도 횟수 (@Min(1), @Max(10))
    original-file-name-truncate-length: 50  # int - 원본 파일명 자르기 길이 (@Min(10), @Max(200))
    forbidden-characters:             # List<String> - 금지 문자들
      - "<"
      - ">"
      - ":"
      - "\""
      - "|"
      - "?"
      - "*"
      - "/"
      - "\\"
      - "\0"
    reserved-names:                   # List<String> - 예약된 파일명 (Windows 호환성)
      - "CON"
      - "PRN"
      - "AUX"
      - "NUL"
      - "COM1"
      - "COM2"
      # ... COM9, LPT1~LPT9
```

### NamingStrategy 열거형
```java
public enum NamingStrategy {
    TIMESTAMP_UUID,      // 타임스탬프 + UUID 조합 (기본): 20231109_143022_a1b2c3d4.jpg
    UUID_ONLY,          // UUID만 사용: a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
    TIMESTAMP_SEQUENCE, // 타임스탬프 + 순번: 20231109_143022_001.jpg
    ORIGINAL_WITH_UUID  // 원본 파일명 보존 + UUID: originalfile_a1b2c3d4.jpg
}
```

**검증**: Jakarta Validation으로 모든 필드 값 검증

## 7️⃣ DebugProperties (`web-starter.debug`)

### 기본 디버그 설정
```yaml
web-starter:
  debug:
    enabled: false                    # Boolean - 디버그 모드 활성화 (기본값: false)
    log-requests: false               # Boolean - 요청/응답 로깅 (기본값: false)
    log-request-body: false           # Boolean - 요청 본문 로깅 (기본값: false)
    log-response-body: false          # Boolean - 응답 본문 로깅 (기본값: false)
    log-performance-metrics: false    # Boolean - 성능 메트릭 로깅 (기본값: false)
    log-detailed-exceptions: false    # Boolean - 상세 예외 로깅 (기본값: false)
    log-bean-registration: false      # Boolean - 빈 등록 정보 로깅 (기본값: false)
    log-configuration: false          # Boolean - 구성 설정 로깅 (기본값: false)
    log-level: "DEBUG"                # String - 로그 레벨 (기본값: "DEBUG")
    max-request-body-log-size: 1024   # int - 최대 요청 본문 로그 크기(바이트)
    max-response-body-log-size: 1024  # int - 최대 응답 본문 로그 크기(바이트)
```

### Performance 설정
```yaml
web-starter:
  debug:
    performance:
      enabled: true                   # Boolean - 성능 메트릭 활성화 (기본값: true)
      slow-request-threshold: 5000    # long - 느린 요청 임계값(ms) (기본값: 5000ms = 5초)
      performance-alert-threshold: 1000  # long - 성능 경고 임계값(ms) (기본값: 1000ms = 1초)
      collect-metrics: true           # Boolean - 성능 메트릭 수집 (기본값: true)
      large-request-size-threshold: 1048576   # long - 큰 요청 크기 임계값(바이트) (기본값: 1MB)
      large-response-size-threshold: 1048576  # long - 큰 응답 크기 임계값(바이트) (기본값: 1MB)
```

## 📝 완전한 설정 예제

### 프로덕션 환경 예제
```yaml
web-starter:
  enabled: true
  mode: HEXAGONAL

  # API 응답과 예외 처리만 활성화
  response-toggle:
    enabled: true
  exception-toggle:
    enabled: true
  cors-toggle:
    enabled: true
  file-toggle:
    enabled: false
  debug-toggle:
    enabled: false

  response:
    enabled: true
    wrap-response: true
    include-request-id: true
    include-timestamp: true

  cors:
    enabled: true
    allowed-origins:
      - "https://mydomain.com"
      - "https://api.mydomain.com"
    allowed-methods:
      - "GET"
      - "POST"
      - "PUT"
      - "DELETE"
    allowed-headers:
      - "Content-Type"
      - "Authorization"
    allow-credentials: true
    max-age: 1h

  pagination:
    enabled: true
    default-size: 10
    max-size: 50
    max-elements: 1000
```

### 개발 환경 예제 (모든 기능 활성화)
```yaml
web-starter:
  enabled: true
  mode: TRADITIONAL

  # 모든 기능 활성화
  response-toggle:
    enabled: true
  exception-toggle:
    enabled: true
  cors-toggle:
    enabled: true
  file-toggle:
    enabled: true
  debug-toggle:
    enabled: true

  file-storage:
    enabled: true
    upload:
      max-file-size: "50MB"
      allowed-extensions:
        - "jpg"
        - "png"
        - "pdf"
        - "docx"
    local:
      base-path: "./dev-uploads"

  file-naming:
    strategy: TIMESTAMP_UUID
    timestamp-pattern: "yyyyMMdd_HHmmss"
    uuid-length: 12

  debug:
    enabled: true
    log-requests: true
    log-request-body: true
    log-response-body: true
    log-performance-metrics: true
    log-configuration: true
    performance:
      slow-request-threshold: 3000
      performance-alert-threshold: 500
```

## 🔧 Bean 조건 매핑

각 Properties가 영향을 주는 Bean들:

### WebStarterProperties
- `WebStarterAutoConfiguration` 전체 활성화
- 모든 편의 메서드를 통한 기능별 조건 체크

### ResponseProperties
- `ResponseAdvice` Bean 동작 제어
- `webStarterWebMvcConfigurer`의 MessageConverter 설정

### CorsProperties
- `corsConfigurationSource` Bean 등록
- `webStarterWebMvcConfigurer`의 CORS 설정

### FileStorageProperties
- `fileValidationService` Bean 등록
- `localFileStorageService` Bean 등록

### FileNamingProperties
- `fileNameGenerator` Bean 등록

### DebugProperties
- `requestLoggingFilter` Bean 등록
- `debugInfoLogger` Bean 등록

## ⚠️ 주의사항

### 1. 조건 체크 순서
Properties의 조건 체크는 다음 순서로 이루어집니다:
1. 루트 `web-starter.enabled`
2. 기능별 토글 (`*-toggle.enabled`)
3. 개별 Properties의 `enabled` 필드

### 2. Zero Configuration 원칙
- 모든 기본값은 즉시 사용 가능하도록 설정
- `matchIfMissing = true` 활용
- 사용자가 명시적으로 비활성화하지 않는 한 기본 활성화

### 3. 검증 어노테이션
- Jakarta Validation 사용
- 잘못된 설정값에 대한 즉시 피드백
- 범위 제한으로 안정성 보장

이 문서는 실제 Properties 클래스 코드를 완전히 분석하여 작성되었으며, 모든 필드와 기본값을 정확히 반영합니다.