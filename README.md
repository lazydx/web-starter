# Web Starter

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-17+-brightgreen.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5+-brightgreen.svg)](https://spring.io/projects/spring-boot)

Spring Boot starter for standardized web application development with pagination, file handling, exception management, and debugging support.

## 🚀 Quick Start

### 1. Add Dependency

#### Gradle
```gradle
implementation 'io.github.lazydx:web-starter:1.1.0'
```

#### Maven
```xml
<dependency>
    <groupId>io.github.lazydx</groupId>
    <artifactId>web-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. Configuration
Add configuration to your `application.yml`:

```yaml
web-starter:
  enabled: true
  
  # Response standardization
  response:
    enabled: true
    wrap-response: true
  
  # CORS configuration
  cors:
    enabled: true
    allowed-origins: "*"
    allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
    allowed-headers: "*"
  
  # Pagination limits
  pagination:
    enabled: true
    max-size: 100
    default-size: 20
    max-elements: 5000
  
  # File storage
  file-storage:
    enabled: true
    local:
      base-path: "./uploads"
  
  # Debug mode (for development)
  debug:
    enabled: false
    log-requests: false
    log-request-body: false
    log-response-body: false
    log-performance-metrics: false
```

### 3. Start Using

That's it! Your Spring Boot application now has:

- ✅ **Standardized API responses** - All responses automatically wrapped in `ApiResponse` format
- ✅ **Pagination limits** - Automatic protection against large page requests
- ✅ **File upload/download** - Ready-to-use file management endpoints
- ✅ **Exception handling** - Consistent error responses across your application
- ✅ **CORS support** - Configurable cross-origin resource sharing
- ✅ **Debug logging** - Detailed request/response logging for development

## 📋 Features

### 🔄 Standardized API Responses

All your API responses are automatically wrapped in a consistent format:

```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2025-08-02T12:00:00Z",
  "requestId": "uuid-string"
}
```

Error responses:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed"
  },
  "timestamp": "2025-08-02T12:00:00Z",
  "requestId": "uuid-string"
}
```

### 📄 Pagination Protection

Automatic pagination limits prevent performance issues:

```java
@GetMapping("/users")
public Page<User> getUsers(Pageable pageable) {
    // pageable.getPageSize() will never exceed your configured max-size
    return userService.findAll(pageable);
}
```

### 📁 File Management

Built-in file upload and download endpoints:

```bash
# Upload file
curl -X POST "http://localhost:8080/api/files/upload" \
  -F "file=@document.pdf"

# Download file
curl "http://localhost:8080/api/files/download/filename.pdf"
```

### ⚠️ Exception Handling

Use standardized exception classes:

```java
import com.ldx.webstarter.exception.BusinessException;
import com.ldx.webstarter.exception.ValidationException;

@RestController
public class UserController {
    
    @PostMapping("/users")
    public User createUser(@RequestBody CreateUserRequest request) {
        if (request.getEmail() == null) {
            throw new ValidationException("EMAIL_REQUIRED", "Email is required");
        }
        
        if (userService.emailExists(request.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email already exists");
        }
        
        return userService.create(request);
    }
}
```

### 🐛 Debug Mode

Enable detailed logging for development:

```yaml
web-starter:
  debug:
    enabled: true
    log-requests: true
    log-request-body: true
    log-response-body: true
    log-performance-metrics: true
```

This will log detailed information about every HTTP request and response, including:
- Request/response headers and bodies
- Performance metrics and timing
- Slow request alerts (>1s warnings, >5s errors)
- Memory and processing statistics

## 🔧 Configuration Reference

### Core Settings
```yaml
web-starter:
  enabled: true  # Enable/disable the entire starter
```

### Response Configuration
```yaml
web-starter:
  response:
    enabled: true          # Enable response wrapping
    wrap-response: true    # Wrap all responses in ApiResponse format
```

### Pagination Configuration
```yaml
web-starter:
  pagination:
    enabled: true          # Enable pagination limits
    default-size: 20       # Default page size
    max-size: 100         # Maximum allowed page size
    max-elements: 5000    # Maximum total elements allowed
    page-parameter: "page"  # Page parameter name
    size-parameter: "size"  # Size parameter name
    sort-parameter: "sort"  # Sort parameter name
```

### CORS Configuration
```yaml
web-starter:
  cors:
    enabled: true
    allowed-origins: "*"                    # Allowed origins
    allowed-methods: "GET,POST,PUT,DELETE" # Allowed HTTP methods
    allowed-headers: "*"                   # Allowed headers
    allow-credentials: false               # Allow credentials
    max-age: 3600                         # Preflight cache duration
```

### File Storage Configuration
```yaml
web-starter:
  file-storage:
    enabled: true
    upload:
      max-file-size: "10MB"
      max-request-size: "100MB"
      allowed-extensions: ["jpg", "jpeg", "png", "pdf", "doc", "txt"]
      enable-virus-scanning: false
    download:
      enable-range-requests: true
      cache-max-age: 3600
      default-content-type: "application/octet-stream"
    local:
      base-path: "./uploads"
      create-directories: true
```

### Debug Configuration
```yaml
web-starter:
  debug:
    enabled: false                    # Enable debug mode
    log-requests: false              # Log HTTP requests
    log-request-body: false          # Log request bodies
    log-response-body: false         # Log response bodies
    log-performance-metrics: false   # Log performance metrics
    log-detailed-exceptions: false   # Log detailed exception stack traces
    log-bean-registration: false     # Log Spring bean registration
    log-configuration: false         # Log configuration on startup
    log-level: "DEBUG"              # Log level for debug messages
    max-request-body-log-size: 1024  # Max request body size to log (bytes)
    max-response-body-log-size: 1024 # Max response body size to log (bytes)
```

## 🛠️ Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/ldx/web-starter.git
cd web-starter

# Build and install to local Maven repository
./gradlew installToMavenLocal
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run integration tests only
./gradlew test --tests "*IntegrationTest*"
```

### Publishing

```bash
# Publish to local repository
./gradlew publishToMavenLocal

# Publish to remote repository (configure credentials first)
./gradlew publish
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- 📫 Email: dev@ldx.com
- 🐛 Issues: [GitHub Issues](https://github.com/ldx/web-starter/issues)
- 📖 Documentation: [Wiki](https://github.com/ldx/web-starter/wiki)
- 💬 Discussions: [GitHub Discussions](https://github.com/ldx/web-starter/discussions)

## 🔄 Version History

### v1.1.0 (2025-08-02) - Latest
- ✅ **Enhanced**: Advanced debug mode with request/response logging
- ✅ **Enhanced**: Performance metrics and slow request detection
- ✅ **Enhanced**: Improved Maven repository integration
- ✅ **Enhanced**: Better error handling and logging
- ✅ **Added**: Comprehensive configuration validation
- ✅ **Added**: Production-ready build and deployment setup
- ✅ **Improved**: Documentation and developer guides

### v1.0.0 (2025-08-02)
- ✅ **Fixed**: Pagination limit functionality
- ✅ **Fixed**: File download functionality  
- ✅ **Fixed**: BusinessException accessibility
- ✅ **Added**: Public API exception classes
- ✅ **Added**: Maven repository support
- ✅ **Added**: Debug mode with detailed logging
- ✅ **Added**: Performance monitoring
- ✅ **Added**: Comprehensive documentation
- ✅ **Improved**: Developer experience and reliability

### v0.0.1-SNAPSHOT (Initial)
- Standard response format support
- Basic pagination limiting
- CORS configuration automation
- Global exception handling