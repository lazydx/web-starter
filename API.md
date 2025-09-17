# Web Starter API 가이드 - 실제 구현 분석

## 개요

이 문서는 web-starter 프로젝트의 실제 구현된 모든 Public API와 메서드들을 코드 분석을 통해 정리한 완전한 API 가이드입니다.

## 🎯 API 구조 개요

### REST 컨트롤러
1. **FileUploadController** - 파일 업로드 관련 API
2. **FileDownloadController** - 파일 다운로드 관련 API

### 서비스 인터페이스
1. **FileStorageService** - 파일 저장소 서비스
2. **FileValidationService** - 파일 검증 서비스
3. **FileNameGenerator** - 파일명 생성기

### 응답 모델
1. **ApiResponse<T>** - 표준 API 응답 래퍼
2. **ErrorResponse** - 에러 응답 구조체
3. **FileMetadata** - 파일 메타데이터

## 📁 파일 업로드 API

### 기본 정보
- **기본 경로**: `/api/files`
- **컨트롤러**: `FileUploadController`
- **응답 형식**: 모든 응답이 `ApiResponse<T>`로 자동 래핑됨

### 1. 단일 파일 업로드

```http
POST /api/files/upload
Content-Type: multipart/form-data
```

**Request Parameters:**
- `file` (required): MultipartFile - 업로드할 파일
- `directory` (optional): String - 저장할 디렉토리

**Response:** `ApiResponse<FileMetadata>`
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "originalFileName": "document.pdf",
    "storedFileName": "20231109_143022_a1b2c3d4.pdf",
    "contentType": "application/pdf",
    "size": 1024576,
    "extension": "pdf",
    "storagePath": "uploads/20231109_143022_a1b2c3d4.pdf",
    "storageType": "LOCAL",
    "uploadedAt": "2023-11-09T14:30:22",
    "checksum": "d41d8cd98f00b204e9800998ecf8427e"
  },
  "timestamp": "2023-11-09T14:30:22Z",
  "requestId": "req-12345678"
}
```

**구현 메서드:**
```java
@PostMapping("/upload")
public ResponseEntity<FileMetadata> uploadFile(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "directory", required = false) String directory
)
```

### 2. 다중 파일 업로드

```http
POST /api/files/upload/multiple
Content-Type: multipart/form-data
```

**Request Parameters:**
- `files` (required): MultipartFile[] - 업로드할 파일 배열
- `directory` (optional): String - 저장할 디렉토리

**Response:** `ApiResponse<List<FileMetadata>>`
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "originalFileName": "file1.jpg",
      "storedFileName": "20231109_143025_b2c3d4e5.jpg",
      "contentType": "image/jpeg",
      "size": 2048576,
      "extension": "jpg",
      "storagePath": "uploads/20231109_143025_b2c3d4e5.jpg",
      "storageType": "LOCAL",
      "uploadedAt": "2023-11-09T14:30:25"
    }
  ],
  "timestamp": "2023-11-09T14:30:25Z",
  "requestId": "req-12345679"
}
```

**오류 처리:**
- 일부 파일만 실패한 경우: 성공한 파일들만 반환
- 모든 파일이 실패한 경우: `BusinessException` 발생

### 3. 파일 메타데이터 조회

```http
GET /api/files/{filename}/metadata
```

**Path Parameters:**
- `filename` (required): String - 파일명 또는 저장 경로

**Response:** `ApiResponse<FileMetadata>`

**구현 메서드:**
```java
@GetMapping("/{filename}/metadata")
public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable String filename)
```

### 4. 파일 삭제

```http
DELETE /api/files/{filename}
```

**Path Parameters:**
- `filename` (required): String - 파일명 또는 저장 경로

**Response:** `ApiResponse<Void>` (204 No Content)

**구현 메서드:**
```java
@DeleteMapping("/{filename}")
public ResponseEntity<Void> deleteFile(@PathVariable String filename)
```

### 5. 파일 존재 확인

```http
GET /api/files/{filename}/exists
```

**Path Parameters:**
- `filename` (required): String - 파일명 또는 저장 경로

**Response:** `ApiResponse<Boolean>`
```json
{
  "success": true,
  "data": true,
  "timestamp": "2023-11-09T14:30:30Z",
  "requestId": "req-12345680"
}
```

## 📥 파일 다운로드 API

### 기본 정보
- **기본 경로**: `/api/files`
- **컨트롤러**: `FileDownloadController`
- **응답 형식**: Resource (바이너리 파일 데이터)

### 1. 저장 경로로 다운로드 (권장)

```http
GET /api/files/download?path={storagePath}&inline={boolean}
```

**Query Parameters:**
- `path` (required): String - 파일의 저장 경로 (FileMetadata.storagePath)
- `inline` (optional): Boolean - 인라인 표시 여부 (기본값: false)

**Response:** Resource (파일 바이너리)
- **Content-Type**: 파일의 실제 MIME 타입
- **Content-Disposition**: attachment 또는 inline
- **Cache-Control**: 설정에 따른 캐시 헤더

**구현 메서드:**
```java
@GetMapping("/download")
public ResponseEntity<Resource> downloadFileByStoragePath(
    @RequestParam("path") String storagePath,
    @RequestParam(value = "inline", defaultValue = "false") boolean inline,
    HttpServletRequest request
)
```

### 2. 파일 ID로 다운로드

```http
GET /api/files/download/id/{fileId}?inline={boolean}
```

**Path Parameters:**
- `fileId` (required): String - 파일 ID (FileMetadata.id)

**Query Parameters:**
- `inline` (optional): Boolean - 인라인 표시 여부

**구현 메서드:**
```java
@GetMapping("/download/id/{fileId}")
public ResponseEntity<Resource> downloadFileById(
    @PathVariable String fileId,
    @RequestParam(value = "inline", defaultValue = "false") boolean inline,
    HttpServletRequest request
)
```

### 3. 파일명으로 다운로드

```http
GET /api/files/download/{filename:.+}?inline={boolean}
```

**Path Parameters:**
- `filename` (required): String - 파일명 (확장자 포함)

**구현 메서드:**
```java
@GetMapping("/download/{filename:.+}")
public ResponseEntity<Resource> downloadFile(
    @PathVariable String filename,
    @RequestParam(value = "inline", defaultValue = "false") boolean inline,
    HttpServletRequest request
)
```

### 4. 파일 인라인 보기

```http
GET /api/files/view/{filename}
```

**Path Parameters:**
- `filename` (required): String - 파일명

**기능**: `downloadFile(filename, true, request)`와 동일
- Content-Disposition이 inline으로 설정됨
- 브라우저에서 직접 표시 가능한 파일들 (이미지, PDF 등)

### 5. 파일 스트리밍 (Range 요청 지원)

```http
GET /api/files/stream/{filename}
Range: bytes=0-1023
```

**Path Parameters:**
- `filename` (required): String - 파일명

**Request Headers:**
- `Range` (optional): String - 요청할 바이트 범위

**Response:**
- **200 OK**: 전체 파일
- **206 Partial Content**: Range 요청 시
- **416 Range Not Satisfiable**: 잘못된 Range 요청

**구현 메서드:**
```java
@GetMapping("/stream/{filename}")
public ResponseEntity<Resource> streamFile(
    @PathVariable String filename,
    HttpServletRequest request,
    @RequestHeader(value = "Range", required = false) String rangeHeader
)
```

## 🔧 FileStorageService Interface

### 파일 저장 메서드

```java
// MultipartFile 저장
FileMetadata store(MultipartFile file);
FileMetadata store(MultipartFile file, String directory);

// InputStream 저장
FileMetadata store(String filename, InputStream inputStream, long size, String contentType);
FileMetadata store(String filename, InputStream inputStream, long size, String contentType, String directory);
```

### 파일 로드 메서드

```java
// Resource 로드
Resource loadAsResource(String filename);
Resource loadAsResource(FileMetadata fileMetadata);

// InputStream 로드
InputStream loadAsInputStream(String filename);
InputStream loadAsInputStream(FileMetadata fileMetadata);
```

### 파일 관리 메서드

```java
// 파일 존재 확인
boolean exists(String filename);
boolean exists(FileMetadata fileMetadata);

// 파일 삭제
void delete(String filename);
void delete(FileMetadata fileMetadata);

// 기타
String getStorageType();
FileMetadata getFileMetadata(String filename);
```

## 🛡️ FileValidationService API

### 파일 검증 메서드

```java
// 전체 파일 검증
public void validateFile(MultipartFile file)

// 개별 검증 메서드
public void validateFileName(String filename)
public void validateFileSize(long fileSize)
public void validateFileExtension(String filename)
public void validateMimeType(String contentType)
public void validateFileContent(MultipartFile file)
public void validateFileContent(InputStream inputStream)
```

### 검증 규칙

1. **파일명 검증**:
   - 빈 파일명 금지
   - 경로 순회 패턴 (`..`) 금지
   - 경로 구분자 (`/`, `\`) 금지
   - 특수 문자 (`<`, `>`, `:`, `"`, `|`, `?`, `*`) 금지

2. **파일 크기 검증**:
   - 설정된 최대 크기 초과 시 예외
   - DataSize 파싱 지원 ("10MB", "1GB" 등)

3. **확장자 검증**:
   - 허용된 확장자 목록 확인
   - 대소문자 무시

4. **MIME 타입 검증**:
   - 허용된 MIME 타입 목록 확인

5. **파일 내용 검증** (바이러스 스캔 활성화 시):
   - 악성 패턴 탐지
   - PHP, JavaScript, 스크립트 패턴 검사

### 예외 처리

```java
public static class FileValidationException extends RuntimeException {
    public FileValidationException(String message)
    public FileValidationException(String message, Throwable cause)
}
```

## 🏷️ FileNameGenerator Interface

### 파일명 생성 메서드

```java
// 고유 파일명 생성
String generateUniqueFilename(String originalFilename, String directory);

// 확장자 추출
String extractFileExtension(String filename);
```

### 구현체: DefaultFileNameGenerator

4가지 네이밍 전략 지원:

1. **TIMESTAMP_UUID** (기본):
   ```
   20231109_143022_a1b2c3d4.pdf
   ```

2. **UUID_ONLY**:
   ```
   a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf
   ```

3. **TIMESTAMP_SEQUENCE**:
   ```
   20231109_143022_001.pdf
   ```

4. **ORIGINAL_WITH_UUID**:
   ```
   originalfile_a1b2c3d4.pdf
   ```

## 📊 응답 모델

### ApiResponse<T>

```java
public class ApiResponse<T> {
    private final boolean success;        // 성공/실패 여부
    private final T data;                // 응답 데이터
    private final ErrorResponse error;   // 에러 정보 (실패 시)
    private final LocalDateTime timestamp; // 응답 시간
    private final String requestId;      // 요청 ID (UUID)

    // 정적 팩토리 메서드
    public static <T> ApiResponse<T> success(T data)
    public static ApiResponse<Void> success()
    public static ApiResponse<Void> error(ErrorResponse error)
}
```

### FileMetadata

```java
public class FileMetadata {
    private String id;                   // 파일 고유 ID
    private String originalFileName;     // 원본 파일명
    private String storedFileName;       // 저장된 파일명
    private String contentType;          // MIME 타입
    private long size;                   // 파일 크기 (바이트)
    private String extension;            // 파일 확장자
    private String storagePath;          // 저장 경로
    private String storageType;          // 저장소 타입 ("LOCAL", "AZURE" 등)
    private LocalDateTime uploadedAt;    // 업로드 시간
    private String uploadedBy;           // 업로드한 사용자
    private String checksum;             // 파일 체크섬 (MD5)

    // 편의 메서드
    public String getFormattedSize()     // 사람이 읽기 쉬운 크기 (1.2 MB)
    public String getFormattedSize(int bytesPerUnit) // 단위 지정 가능
}
```

### ErrorResponse

```java
public class ErrorResponse {
    private String code;                 // 에러 코드
    private String message;              // 에러 메시지
    private List<String> details;        // 상세 에러 목록
    private LocalDateTime timestamp;     // 에러 발생 시간

    // 정적 팩토리 메서드
    public static ErrorResponse of(String code, String message)
    public static ErrorResponse of(String code, String message, List<String> details)
}
```

## 🚨 예외 처리

### 전역 예외 처리기: GlobalExceptionHandler

**처리하는 예외 타입:**

1. **비즈니스 예외**:
   - `ApplicationBusinessException` → 400 Bad Request
   - `BusinessException` → 400 Bad Request (Traditional 모드)
   - `WebStarterException` → 400 Bad Request
   - `ValidationException` → 422 Unprocessable Entity

2. **파일 관련 예외**:
   - `FileNotFoundException` → 404 Not Found
   - `FileValidationException` → 400 Bad Request (FileUploadController에서 변환)

3. **표준 Java 예외**:
   - `IllegalArgumentException` → 400 Bad Request
   - `NullPointerException` → 400 Bad Request
   - `IllegalStateException` → 409 Conflict

4. **Spring 웹 예외**:
   - `MethodArgumentNotValidException` → 422 Unprocessable Entity
   - `HttpRequestMethodNotSupportedException` → 405 Method Not Allowed
   - `NoHandlerFoundException` → 404 Not Found
   - `NoResourceFoundException` → 404 Not Found

5. **일반 예외**:
   - `Exception` → 500 Internal Server Error

### Mode별 예외 처리

**Hexagonal 모드에서의 아키텍처 위반 검증**:
```java
if (properties.getMode() == WebStarterProperties.Mode.HEXAGONAL) {
    // Domain BusinessException이 직접 GlobalExceptionHandler에 도달하면
    throw new IllegalStateException(
        "Architecture violation in HEXAGONAL mode: " +
        "Domain layer BusinessException must be converted to ApplicationBusinessException"
    );
}
```

## 🔧 설정 기반 동작

### 파일 업로드 제한

```yaml
web-starter:
  file-storage:
    upload:
      max-file-size: "10MB"
      allowed-extensions: ["jpg", "png", "pdf"]
      allowed-mime-types: ["image/jpeg", "image/png", "application/pdf"]
```

### 다운로드 최적화

```yaml
web-starter:
  file-storage:
    download:
      enable-range-requests: true
      cache-max-age: 3600
```

### 파일명 생성 전략

```yaml
web-starter:
  file-naming:
    strategy: TIMESTAMP_UUID
    uuid-length: 8
    separator: "_"
```

## 📝 사용 예제

### 1. 파일 업로드 (JavaScript)

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('directory', 'documents');

fetch('/api/files/upload', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(apiResponse => {
    if (apiResponse.success) {
        const fileMetadata = apiResponse.data;
        console.log('Upload successful:', fileMetadata);
        console.log('Download URL:', `/api/files/download?path=${fileMetadata.storagePath}`);
    }
});
```

### 2. 파일 다운로드 링크

```html
<!-- 다운로드 -->
<a href="/api/files/download?path=uploads/20231109_143022_a1b2c3d4.pdf">
    문서 다운로드
</a>

<!-- 인라인 보기 -->
<a href="/api/files/view/20231109_143022_a1b2c3d4.pdf" target="_blank">
    문서 보기
</a>
```

### 3. Java 서비스에서 사용

```java
@Service
public class DocumentService {

    @Autowired
    private FileStorageService fileStorageService;

    public String uploadDocument(MultipartFile file) {
        FileMetadata metadata = fileStorageService.store(file, "documents");
        return metadata.getStoragePath(); // 다운로드 시 사용할 경로
    }

    public Resource downloadDocument(String storagePath) {
        return fileStorageService.loadAsResource(storagePath);
    }
}
```

이 문서는 실제 코드를 완전히 분석하여 작성되었으며, web-starter의 모든 Public API와 메서드를 정확히 반영합니다.