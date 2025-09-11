# 🌐 Web Starter Configuration Guide

## 📋 Overview

LDX Web Starter provides essential web development features such as API response standardization, global exception handling, CORS configuration, and file processing with Zero Configuration for Spring Boot applications.

**Configuration Prefix**: `ldx.web`

## 🚀 Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.ldx</groupId>
    <artifactId>web-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Zero Configuration (No Configuration Required)

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Auto-enabled Features:**
- ✅ API response standardization (ApiResponse wrapping)
- ✅ Global exception handling (GlobalExceptionHandler)
- ✅ CORS configuration
- ✅ Pagination support

## 🎯 Feature-based Configuration Guide

### 1. API Response Standardization

#### 1.1 Response Standardization Configuration

```yaml
ldx:
  web:
    response:
      enabled: true
      wrap-response: true
      include-request-id: true
      include-timestamp: true
```

#### 1.2 Required Java Code

**Create Basic Controller:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping
    public List<User> getUsers() {
        // ResponseAdvice automatically wraps with ApiResponse<List<User>>
        return userService.findAll();
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        // Automatically wrapped with ApiResponse<User>
        return userService.save(user);
    }
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // Exception handling → processed by GlobalExceptionHandler
        return userService.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
```

#### 1.3 Result

**Success Response (Auto-wrapped):**
```json
{
  "success": true,
  "data": [
    {"id": 1, "name": "John", "email": "john@example.com"},
    {"id": 2, "name": "Jane", "email": "jane@example.com"}
  ],
  "error": null,
  "timestamp": "2025-01-10T10:00:00Z",
  "requestId": "uuid-string"
}
```

**Error Response:**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found",
    "details": null
  },
  "timestamp": "2025-01-10T10:00:00Z",
  "requestId": "uuid-string"
}
```

### 2. Global Exception Handling

#### 2.1 Exception Handling Configuration

```yaml
ldx:
  web:
    exception-toggle:
      enabled: true
    mode: TRADITIONAL  # TRADITIONAL | HEXAGONAL
```

#### 2.2 Required Java Code

**Create Custom Exception Classes:**
```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}
```

**Throw Exceptions in Business Logic:**
```java
@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
    
    public User save(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new InvalidUserDataException("Email is required.");
        }
        return userRepository.save(user);
    }
}
```

#### 2.3 Result

**Auto-handled Exceptions:**
- `UserNotFoundException` → 404 Not Found
- `InvalidUserDataException` → 400 Bad Request
- `@Valid` validation failures → 422 Unprocessable Entity
- `IllegalArgumentException` → 400 Bad Request
- Other exceptions → 500 Internal Server Error

**Special Handling in Hexagonal Mode:**
- Architecture violation exception thrown when Domain Layer BusinessException directly propagates

### 3. CORS Configuration

#### 3.1 CORS Configuration

```yaml
ldx:
  web:
    cors:
      enabled: true
      allowed-origins: 
        - "https://frontend.example.com"
        - "https://admin.example.com"
      allowed-methods: ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
      allowed-headers: ["*"]
      allow-credentials: true
      max-age: PT30M
      path-pattern: "/api/**"
```

#### 3.2 Required Java Code

```java
# CORS configuration is auto-applied. No additional code required
```

#### 3.3 Result

**Auto-applied CORS Headers:**
```http
Access-Control-Allow-Origin: https://frontend.example.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 1800
```

### 4. File Upload/Download

#### 4.1 File Processing Configuration

```yaml
ldx:
  web:
    file-toggle:
      enabled: true
    file-storage:
      enabled: true
      local:
        base-path: "./uploads"
        create-directories: true
      upload:
        max-file-size: "10MB"
        max-request-size: "100MB"
        allowed-extensions: ["jpg", "jpeg", "png", "gif", "pdf", "doc", "docx"]
        allowed-mime-types: 
          - "image/jpeg"
          - "image/png"
          - "application/pdf"
        temp-dir: "${java.io.tmpdir}/webstarter-uploads"
      download:
        enable-range-requests: true
        cache-max-age: 3600
```

#### 4.2 Required Java Code

**File Processing Controllers are Auto-registered:**
```java
# Following endpoints are auto-enabled:
# POST /api/files/upload          - Single file upload
# POST /api/files/upload/multiple - Multiple file upload  
# GET  /api/files/{filename}      - File download
# GET  /api/files/{filename}/metadata - File metadata
# DELETE /api/files/{filename}    - File deletion
```

**For Custom File Processing:**
```java
@RestController
@RequestMapping("/api/custom-files")
public class CustomFileController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileStorageService.store(file);
            return ResponseEntity.ok(new UploadResponse(fileName, file.getSize()));
        } catch (Exception e) {
            throw new FileUploadException("File upload failed: " + e.getMessage());
        }
    }
}
```

#### 4.3 Result

**File Upload Success Response:**
```json
{
  "success": true,
  "data": {
    "fileName": "document_20250110_100000_abc123.pdf",
    "originalFileName": "document.pdf",
    "size": 2048576,
    "contentType": "application/pdf",
    "uploadTime": "2025-01-10T10:00:00Z"
  },
  "error": null
}
```

**Auto-applied Validations:**
- File extension validation
- MIME type validation
- File size limits
- File header content validation (security)

### 5. Pagination

#### 5.1 Pagination Configuration

```yaml
ldx:
  web:
    pagination:
      enabled: true
      default-size: 20
      max-size: 100
      max-elements: 5000
      page-parameter: "page"
      size-parameter: "size"
      sort-parameter: "sort"
```

#### 5.2 Required Java Code

**Pagination-enabled Controller:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping
    public Page<User> getUsers(Pageable pageable) {
        // PaginationArgumentResolver auto-applies limits
        // Max size and max elements automatically validated
        return userService.findAll(pageable);
    }
    
    @GetMapping("/search")
    public Page<User> searchUsers(
            @RequestParam String keyword, 
            Pageable pageable) {
        return userService.findByNameContaining(keyword, pageable);
    }
}
```

#### 5.3 Result

**Pagination Request:**
```http
GET /api/users?page=0&size=10&sort=name,asc&sort=email,desc
```

**Pagination Response:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageable": {
      "sort": {"sorted": true, "unsorted": false},
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 150,
    "totalPages": 15,
    "last": false,
    "first": true
  }
}
```

### 6. Debug Mode

#### 6.1 Debug Configuration

```yaml
ldx:
  web:
    debug-toggle:
      enabled: true
    debug:
      enabled: true
      log-requests: true
      log-request-body: true
      log-response-body: true
      log-performance-metrics: true
      log-detailed-exceptions: true
      performance:
        slow-request-threshold: 5000      # Detect slow requests > 5 seconds
        performance-alert-threshold: 1000 # Log performance for requests > 1 second
        collect-metrics: true
```

#### 6.2 Required Java Code

```java
# Debug features are auto-applied. No additional code required
```

#### 6.3 Result

**Request/Response Logging Example:**
```
[REQUEST ] GET /api/users?page=0&size=10
[HEADERS ] Content-Type: application/json, User-Agent: Mozilla/5.0...
[BODY    ] (empty)
[RESPONSE] 200 OK (took 234ms)
[BODY    ] {"success":true,"data":[...],"error":null}
[PERF    ] Request processed in 234ms (threshold: 1000ms)
```

**Performance Warning Logging:**
```
[SLOW    ] ⚠️ Slow request detected: GET /api/users/export took 6543ms (threshold: 5000ms)
[LARGE   ] ⚠️ Large response detected: 2.4MB (request: GET /api/users/report)
```

## 🔧 Advanced Configuration

### 1. Mode Configuration

```yaml
ldx:
  web:
    mode: HEXAGONAL  # TRADITIONAL | HEXAGONAL
```

**Traditional Mode**: Free use of web features across all layers
**Hexagonal Mode**: Restricted use of web features in Domain Layer, architecture violation detection

### 2. Fine-grained FeatureToggle Control

```yaml
ldx:
  web:
    enabled: true
    response-toggle:
      enabled: true
    exception-toggle:
      enabled: true
    cors-toggle:
      enabled: false      # Disable CORS
    file-toggle:
      enabled: false      # Disable file processing
    debug-toggle:
      enabled: true
```

### 3. Advanced File Processing Configuration

```yaml
ldx:
  web:
    file-storage:
      upload:
        allowed-extensions: ["jpg", "png"]  # Images only
        max-file-size: "5MB"
        enable-virus-scanning: true         # Virus scanning (future implementation)
      download:
        enable-range-requests: true         # HTTP Range request support
        cache-max-age: 86400               # 24-hour cache
```

### 4. Custom Bean Registration

```yaml
# application.yml configuration remains the same
```

**Custom File Storage Service:**
```java
@Component
public class CustomFileStorageService implements FileStorageService {
    
    @Override
    public String store(MultipartFile file) {
        // Store files in cloud storage (S3, Azure Blob, etc.)
        return uploadToCloud(file);
    }
    
    @Override
    public Resource loadAsResource(String fileName) {
        // Download files from cloud
        return downloadFromCloud(fileName);
    }
}
```

**Custom File Name Generator:**
```java
@Component
public class CustomFileNameGenerator implements FileNameGenerator {
    
    @Override
    public String generate(String originalFilename) {
        // Custom file naming logic
        return "custom_" + System.currentTimeMillis() + "_" + originalFilename;
    }
}
```

## ⚠️ Important Notes

### 1. File Upload Limits

**Spring Boot Default Configuration:**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB
```

**Web Starter Additional Limits:**
```yaml
ldx:
  web:
    file-storage:
      upload:
        max-file-size: "5MB"    # More strict limit applied
```

### 2. CORS Security Considerations

**Development Environment:**
```yaml
ldx:
  web:
    cors:
      allowed-origins: ["http://localhost:3000"]
      allow-credentials: false
```

**Production Environment:**
```yaml
ldx:
  web:
    cors:
      allowed-origins: ["https://your-domain.com"]
      allow-credentials: true
      allowed-headers: ["Content-Type", "Authorization"]
```

### 3. Debug Mode Precautions

**Recommended to disable in production:**
```yaml
ldx:
  web:
    debug-toggle:
      enabled: false
    debug:
      log-request-body: false   # Prevent sensitive information logging
      log-response-body: false  # Performance impact from response size
```

## 🔍 Troubleshooting

### 1. API Responses Not Wrapped

**Cause**: Controller method already returns ApiResponse type
**Solution**: Modify to return primitive types (String, List, etc.)

### 2. CORS Errors

**Cause**: Frontend domain not included in allowed-origins
**Solution**: Verify correct domain configuration (including protocol)

### 3. File Upload Failures

**Cause**: File size or extension limits exceeded
**Solution**: Adjust configuration values or validate files before upload

### 4. Pagination Not Working

**Cause**: Missing Spring Data JPA dependency
**Solution**: Add spring-boot-starter-data-jpa dependency

## 📚 Usage Tips

### 1. Using with ResponseEntity

```java
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    User savedUser = userService.save(user);
    // ResponseAdvice wraps only ResponseEntity content
    return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
}
```

### 2. Customizing File Downloads

```java
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Resource resource = fileStorageService.loadAsResource(filename);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(resource);
}
```

### 3. Customizing Exception Handling

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)  // Higher priority than default handler
public class CustomExceptionHandler {
    
    @ExceptionHandler(CustomBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomBusinessException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("CUSTOM_ERROR", e.getMessage()));
    }
}
```

---
*Version: 1.0.0 | Last Updated: 2025-01-10*