# Web Starter - 실제 구현 기능 문서

## 개요

이 문서는 web-starter 프로젝트의 실제 구현된 기능들을 코드 분석을 통해 정리한 문서입니다.

### 버전 정보
- **버전**: 1.1.0
- **패키지**: com.ldx.webstarter
- **Spring Boot Starter** 표준 아키텍처 준수

## 🏗️ 아키텍처 구조

### 메인 자동 설정
- **클래스**: `WebStarterAutoConfiguration`
- **조건**: `@ConditionalOnProperty(prefix = "web-starter", name = "enabled", havingValue = "true", matchIfMissing = true)`
- **Zero Configuration 원칙** 준수 (기본값으로 활성화)

### 컴포넌트 스캔 패키지
```java
@ComponentScan(basePackages = {
    "com.ldx.webstarter.file",                      // File Controllers
    "com.ldx.webstarter.infrastructure.advice",     // ResponseAdvice
    "com.ldx.webstarter.infrastructure.exception",  // GlobalExceptionHandler
    "com.ldx.webstarter"                            // TestController (테스트용)
})
```

## 🎯 핵심 기능

### 1. **아키텍처 모드 지원**
- **Traditional 모드**: 모든 레이어에서 web-starter 자유 사용
- **Hexagonal 모드**: Domain Layer에서 web-starter 사용 제한
- Domain Layer 예외가 직접 전역 핸들러에 도달하면 아키텍처 위반으로 처리

### 2. **FeatureToggle 기반 기능 제어**
```java
// 각 기능별 개별 토글
private FeatureToggle responseToggle = new FeatureToggle(true);     // API 응답 표준화
private FeatureToggle exceptionToggle = new FeatureToggle(true);    // 전역 예외 처리
private FeatureToggle corsToggle = new FeatureToggle(true);         // CORS 설정
private FeatureToggle fileToggle = new FeatureToggle(false);        // 파일 처리 (기본 OFF)
private FeatureToggle debugToggle = new FeatureToggle(false);       // 디버그 기능 (기본 OFF)
```

### 3. **API 응답 표준화**
- **클래스**: `ResponseAdvice` (RestControllerAdvice)
- **기능**: 모든 API 응답을 `ApiResponse<T>` 형식으로 자동 래핑
- **제외 대상**: Resource 타입(파일 다운로드), Actuator 엔드포인트
- **특별 처리**: String 응답의 JSON 변환, void 메서드의 빈 성공 응답

### 4. **전역 예외 처리**
- **클래스**: `GlobalExceptionHandler` (RestControllerAdvice)
- **Mode별 분기**: Traditional/Hexagonal 모드에 따른 다른 예외 처리 전략
- **지원 예외 타입**:
  - `ApplicationBusinessException` (Hexagonal Architecture용)
  - `BusinessException` (Traditional Mode용)
  - `WebStarterException` 계층
  - `ValidationException`
  - 표준 Java 예외들 (`IllegalArgumentException`, `NullPointerException` 등)
  - Spring 웹 예외들 (`MethodArgumentNotValidException`, `NoHandlerFoundException` 등)

### 5. **파일 업로드/다운로드**
- **업로드 컨트롤러**: `FileUploadController`
- **다운로드 컨트롤러**: `FileDownloadController`
- **저장 서비스**: `FileStorageService` 인터페이스
- **구현체**: `LocalFileStorageService`
- **검증 서비스**: `FileValidationService`
- **파일명 생성**: `FileNameGenerator` (4가지 전략 지원)

#### 파일 API 엔드포인트
```
POST /api/files/upload              - 단일 파일 업로드
POST /api/files/upload/multiple     - 다중 파일 업로드
GET  /api/files/{filename}/metadata - 파일 메타데이터 조회
DELETE /api/files/{filename}        - 파일 삭제
GET  /api/files/{filename}/exists   - 파일 존재 확인
```

### 6. **CORS 설정**
- **통합 설정**: `WebMvcConfigurer` Bean을 통한 CORS 설정
- **소스 Bean**: `CorsConfigurationSource` 자동 구성
- **기본 설정**: 모든 오리진(`*`), 표준 HTTP 메서드 허용

### 7. **페이지네이션 지원**
- **리졸버**: `PaginationArgumentResolver`
- **기본 설정**: 페이지 크기 20, 최대 100, 총 5000개 제한
- **파라미터**: page, size, sort

### 8. **디버그 기능**
- **요청 로깅 필터**: `RequestLoggingFilter`
- **성능 모니터링**: 느린 요청 감지 (기본 5초 임계값)
- **상세 로깅**: 요청/응답 본문, 성능 메트릭, 예외 스택 트레이스

## 📋 주요 컴포넌트

### 도메인 모델
- **`ApiResponse<T>`**: 표준 API 응답 래퍼
- **`ErrorResponse`**: 에러 응답 구조체
- **`PageResponse<T>`**: 페이지네이션 응답
- **`FileMetadata`**: 파일 메타데이터 (ID, 원본명, 저장명, 크기, 타입 등)

### 예외 계층
```
WebStarterException (root)
├── BusinessException
├── ValidationException
└── ApplicationBusinessException (Hexagonal Architecture용)

파일 관련:
├── FileNotFoundException
├── FileUploadException
└── FileValidationException
```

### 서비스 인터페이스
- **`FileStorageService`**: 파일 저장/로드/삭제 인터페이스
- **`FileNameGenerator`**: 파일명 생성 전략 인터페이스

## ⚙️ 자동 구성 Bean들

### 핵심 Bean
1. **`webStarterWebMvcConfigurer`**: 통합 WebMvc 설정
2. **`corsConfigurationSource`**: CORS 설정 소스
3. **`fileNameGenerator`**: 파일명 생성기
4. **`fileValidationService`**: 파일 검증 서비스
5. **`localFileStorageService`**: 로컬 파일 저장 서비스
6. **`requestLoggingFilter`**: 요청 로깅 필터
7. **`debugInfoLogger`**: 디버그 정보 로거

### 조건부 Bean 등록
- 모든 Bean은 `@ConditionalOnMissingBean` 적용 (사용자 커스터마이징 우선)
- 기능별 Property 조건 확인
- Zero Configuration 원칙 준수

## 🔧 HttpMessageConverter 최적화

String 타입 응답의 ResponseAdvice 호환성을 위해 MessageConverter 순서 조정:
1. `StringHttpMessageConverter` 제거
2. `MappingJackson2HttpMessageConverter` 이후에 재배치
3. JSON 응답 보장

## 🚀 초기화 프로세스

자동 설정 초기화 시 상세한 로깅 제공:
```
╔═══════════════════════════════════════════════════════════════╗
║                 🚀 LDX WEB STARTER                            ║
║                    Version 1.1.0                               ║
╚═══════════════════════════════════════════════════════════════╝

Configuration Status, Active Features 등 상세 정보 출력
```

## 📊 성능 및 보안

### 성능 최적화
- 불필요한 Bean 생성 방지 (조건부 등록)
- 빠른 시작 시간 (조건 체크 최적화)
- 메모리 효율적인 파일 처리

### 보안 기능
- 파일 업로드 검증 (확장자, MIME 타입, 크기)
- 바이러스 스캔 옵션 제공
- 예약된 파일명 검증 (Windows 호환성)
- 경로 순회 공격 방지

## 📝 사용 방법

### 1. 기본 사용 (Zero Configuration)
```java
// 의존성 추가만으로 모든 기능 자동 활성화
// 별도 설정 없이 바로 사용 가능
```

### 2. 기능별 토글
```yaml
web-starter:
  enabled: true
  mode: TRADITIONAL  # 또는 HEXAGONAL
  response-toggle:
    enabled: true
  file-toggle:
    enabled: true
```

### 3. 컨트롤러에서 사용
```java
@RestController
public class MyController {

    // 자동으로 ApiResponse<String>으로 래핑됨
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    // 파일 업로드 (별도 설정 불필요)
    @Autowired
    private FileStorageService fileStorageService;
}
```

## 🔍 특별한 설계 특징

### 1. Hybrid 접근법
- **MVC 컴포넌트**: `@ComponentScan`으로 자동 등록
- **Service Bean**: 명시적 `@Bean` 등록으로 세밀한 제어

### 2. 아키텍처 위반 방지
Hexagonal 모드에서 Domain Layer의 BusinessException이 직접 GlobalExceptionHandler에 도달하면:
```java
throw new IllegalStateException(
    "Architecture violation in HEXAGONAL mode: " +
    "Domain layer BusinessException must be converted to ApplicationBusinessException " +
    "in Application layer."
);
```

### 3. False Positive 최소화
- 스마트한 조건부 처리
- 정확한 타입 검사
- 실제 구현 기반 문서화

이 문서는 실제 코드 분석을 통해 작성되었으며, web-starter의 모든 구현된 기능을 정확히 반영합니다.