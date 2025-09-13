# Web Starter

A Spring Boot starter library for standardized web application development with common features like API response wrapping, exception handling, file management, and more.

## Features

- **Zero Configuration**: Works out of the box with sensible defaults
- **API Response Standardization**: Automatic wrapping of REST responses
- **Global Exception Handling**: Consistent error response format
- **CORS Configuration**: Flexible cross-origin resource sharing setup
- **File Upload/Download**: Built-in file management capabilities
- **Pagination Support**: Standardized pagination for list endpoints
- **Debug Mode**: Request/response logging for development
- **No Database Required**: Standalone library with no DB dependencies

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.lazydx</groupId>
    <artifactId>web-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.lazydx:web-starter:1.1.1'
```

## Quick Start

### 1. Add dependency to your project

### 2. Application starts with default settings
```java
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. Create a REST controller
```java
@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/users")
    public List<User> getUsers() {
        // Automatically wrapped in ApiResponse
        return userService.findAll();
    }
}
```

## Configuration

### Basic Configuration
```yaml
web-starter:
  enabled: true  # Enable/disable the starter (default: true)
  mode: TRADITIONAL  # or HEXAGONAL (default: TRADITIONAL)
```

### Feature Toggles
```yaml
web-starter:
  response-toggle:
    enabled: true  # API response wrapping (default: true)

  exception-toggle:
    enabled: true  # Global exception handling (default: true)

  cors-toggle:
    enabled: false  # CORS configuration (default: false)

  file-toggle:
    enabled: false  # File upload/download (default: false)

  debug-toggle:
    enabled: false  # Debug logging (default: false)
```

### CORS Configuration
```yaml
web-starter:
  cors-toggle:
    enabled: true
  cors:
    allowed-origins:
      - "http://localhost:3000"
      - "https://yourdomain.com"
    allowed-methods:
      - GET
      - POST
      - PUT
      - DELETE
    allowed-headers:
      - "*"
    allow-credentials: true
    max-age: 3600
```

### File Storage Configuration
```yaml
web-starter:
  file-toggle:
    enabled: true
  file:
    upload-dir: "./uploads"
    max-file-size: "10MB"
    max-request-size: "50MB"
    allowed-extensions:
      - jpg
      - png
      - pdf
      - doc
      - docx
```

### Debug Configuration
```yaml
web-starter:
  debug-toggle:
    enabled: true
  debug:
    log-request: true
    log-response: true
    log-headers: false
    max-payload-length: 1000
```

## API Response Format

All API responses are automatically wrapped in a standardized format:

### Success Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe"
  },
  "timestamp": "2024-01-13T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": ["Field 'email' is required"]
  },
  "timestamp": "2024-01-13T10:30:00"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2024-01-13T10:30:00"
}
```

## Exception Handling

The starter provides automatic exception handling for common scenarios:

- `BusinessException`: Business logic errors (400 Bad Request)
- `NotFoundException`: Resource not found (404 Not Found)
- `ValidationException`: Input validation errors (400 Bad Request)
- `MethodArgumentNotValidException`: Spring validation errors (400 Bad Request)
- `HttpMessageNotReadableException`: Malformed request (400 Bad Request)
- `Exception`: Unexpected errors (500 Internal Server Error)

## File Upload Example

```java
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public FileMetadata uploadFile(@RequestParam("file") MultipartFile file) {
        return fileStorageService.store(file);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource resource = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
}
```

## Pagination Example

```java
@GetMapping("/users")
public Page<User> getUsers(Pageable pageable) {
    // Pageable is automatically resolved from query parameters
    // ?page=0&size=20&sort=name,asc
    return userService.findAll(pageable);
}
```

## License

Apache License 2.0

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and questions, please use the [GitHub Issues](https://github.com/lazydx/web-starter/issues) page.