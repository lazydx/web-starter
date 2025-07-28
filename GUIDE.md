# Web Starter Guide

A starter library that provides common features for Spring Boot web applications.

## 📦 Installation

### Gradle
```gradle
dependencies {
    implementation 'com.ldx:webstarter:1.0.0'
    // or use local JAR file
    implementation files('libs/webstarter-0.0.1-SNAPSHOT.jar')
}
```

### Maven
```xml
<dependency>
    <groupId>com.ldx</groupId>
    <artifactId>webstarter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 Getting Started

### 1. Auto Configuration

The starter is **automatically activated** when you add the JAR to your Spring Boot application. No additional configuration is required.

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 2. Basic Configuration

You can add configuration to `application.yml`:

```yaml
web-starter:
  enabled: true  # Enable/disable all features (default: true)
  
  # Response standardization settings
  response:
    enabled: true
    wrap-response: true
  
  # CORS settings
  cors:
    enabled: true
    allowed-origins: "*"
    allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
    allowed-headers: "*"
    allow-credentials: false
    max-age: 3600
  
  # Pagination settings
  pagination:
    enabled: true
    default-size: 20
    max-size: 100
    max-elements: 5000
  
  # File storage settings
  file-storage:
    enabled: true
    upload:
      max-file-size: 10MB
      max-request-size: 100MB
      allowed-extensions: ["jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt"]
      enable-virus-scanning: false
    local:
      base-path: "./uploads"
    s3:
      enabled: false
      bucket-name: "my-bucket"
      region: "us-east-1"
```

## 📋 Key Features

### 1. Standard Response Format

All API responses are automatically converted to a consistent format and returned with **appropriate HTTP status codes**.

#### Success Response
```json
{
  "success": true,
  "data": { 
    "id": 1,
    "name": "John Doe" 
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "requestId": "uuid"
}
```

#### Error Response
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "Error message",
    "details": ["Detailed error information"]
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "requestId": "uuid"
}
```

#### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": {
      "size": 20,
      "number": 0,
      "totalElements": 100,
      "totalPages": 5
    }
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "requestId": "uuid"
}
```

### 2. Automatic Pagination Limits

Pagination parameters are automatically limited.

```java
@RestController
public class UserController {
    
    @GetMapping("/users")
    public PageResponse<User> getUsers(Pageable pageable) {
        // pageable.getPageSize() is automatically limited to max 100
        Page<User> users = userService.findAll(pageable);
        return PageResponse.of(users);
    }
}
```

**Request Examples:**
```bash
# Normal request
curl "http://localhost:8080/users?page=0&size=20"

# Limit applied - size=150 → actually limited to 100
curl "http://localhost:8080/users?page=0&size=150"
```

### 3. Global Exception Handling

Exceptions are automatically converted to standard error responses and returned with **appropriate HTTP status codes**.

#### HTTP Status Code Mapping

| Exception Type | HTTP Status Code | Description |
|---------------|------------------|-------------|
| `BusinessException` | `400 Bad Request` | Business logic error |
| `MethodArgumentNotValidException` | `422 Unprocessable Entity` | Input validation failure |
| `NotFoundException` | `404 Not Found` | Resource not found |
| Other exceptions | `500 Internal Server Error` | Unexpected server error |

#### Using Business Exceptions
```java
import com.ldx.webstarter.infrastructure.exception.BusinessException;

@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "USER_NOT_FOUND", 
                "User not found."
            ));
    }
}
```

#### Validation Exceptions
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        // @Valid validation failures automatically generate standard error responses
        return userService.create(request);
    }
}

public class CreateUserRequest {
    @NotBlank(message = "Name is required.")
    private String name;
    
    @Email(message = "Invalid email format.")
    private String email;
}
```

### 4. CORS Configuration

Cross-Origin requests are automatically handled.

```yaml
web-starter:
  cors:
    enabled: true
    allowed-origins: 
      - "https://mydomain.com"
      - "https://api.mydomain.com"
    allowed-methods: "GET,POST,PUT,DELETE"
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

### 5. File Upload & Download

Secure file upload and download with validation and multiple storage options.

#### File Upload Example
```java
@RestController
public class DocumentController {
    
    private final FileStorageService fileStorageService;
    private final FileValidationService fileValidationService;
    
    @PostMapping("/documents/upload")
    public FileMetadata uploadDocument(@RequestParam("file") MultipartFile file) {
        // File validation is handled automatically
        return fileStorageService.store(file, "documents");
    }
    
    @PostMapping("/documents/upload/multiple")
    public List<FileMetadata> uploadMultipleDocuments(@RequestParam("files") MultipartFile[] files) {
        return Arrays.stream(files)
            .map(file -> fileStorageService.store(file, "documents"))
            .collect(Collectors.toList());
    }
}
```

#### File Download Example
```java
@GetMapping("/documents/{filename}")
public ResponseEntity<Resource> downloadDocument(@PathVariable String filename) {
    Resource resource = fileStorageService.loadAsResource(filename);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(resource);
}
```

#### Configuration Options
```yaml
web-starter:
  file-storage:
    enabled: true
    upload:
      max-file-size: 10MB              # Maximum file size
      max-request-size: 100MB          # Maximum request size
      allowed-extensions: ["jpg", "png", "pdf", "docx"]  # Allowed file types
      allowed-mime-types: ["image/jpeg", "image/png", "application/pdf"]
      enable-virus-scanning: true      # Basic malware detection
      temp-dir: "/tmp/uploads"         # Temporary upload directory
    download:
      enable-range-requests: true      # Support partial downloads
      cache-max-age: 3600             # Cache duration in seconds
    local:
      base-path: "./uploads"           # Local storage path
      create-directories: true         # Auto-create directories
    s3:
      enabled: false                   # Enable AWS S3 storage
      bucket-name: "my-app-files"     # S3 bucket name
      region: "us-east-1"             # AWS region
      access-key: "${AWS_ACCESS_KEY}" # AWS credentials
      secret-key: "${AWS_SECRET_KEY}"
      path-prefix: "uploads/"         # S3 path prefix
```

#### API Endpoints

The starter automatically provides REST endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/files/upload` | POST | Upload single file |
| `/api/files/upload/multiple` | POST | Upload multiple files |
| `/api/files/download/{filename}` | GET | Download file |
| `/api/files/view/{filename}` | GET | View file inline |
| `/api/files/stream/{filename}` | GET | Stream file with range support |
| `/api/files/{filename}/metadata` | GET | Get file metadata |
| `/api/files/{filename}/exists` | GET | Check file existence |
| `/api/files/{filename}` | DELETE | Delete file |

#### Upload Response Format
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "originalFileName": "document.pdf",
    "storedFileName": "20241201_123456_abc12345.pdf",
    "contentType": "application/pdf",
    "size": 1024000,
    "extension": "pdf",
    "storagePath": "documents/20241201_123456_abc12345.pdf",
    "storageType": "LOCAL",
    "uploadedAt": "2024-12-01T12:34:56",
    "checksum": "d41d8cd98f00b204e9800998ecf8427e"
  }
}
```

#### Error Handling
```json
{
  "success": false,
  "error": {
    "code": "FILE_VALIDATION_ERROR",
    "message": "File extension 'exe' is not allowed. Allowed extensions: [jpg, png, pdf]"
  }
}
```

## 🛠️ Advanced Configuration

### Disabling Individual Features

```yaml
web-starter:
  enabled: true
  response:
    enabled: false  # Disable response wrapping
  cors:
    enabled: false  # Disable CORS
  pagination:
    enabled: false  # Disable pagination limits
```

### Environment-specific Configuration

```yaml
# application-dev.yml
web-starter:
  cors:
    allowed-origins: "*"  # Allow all origins in development

---
# application-prod.yml
web-starter:
  cors:
    allowed-origins: 
      - "https://myapp.com"  # Allow only specific domains in production
```

### Pagination Customization

```yaml
web-starter:
  pagination:
    default-size: 10      # Default page size
    max-size: 50          # Maximum page size
    max-elements: 1000    # Maximum elements
    size-parameter: "pageSize"    # Change size parameter name
    page-parameter: "pageNum"     # Change page parameter name
```

## 📝 Example Code

### Complete REST API Example

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    // List with pagination (automatically applied)
    @GetMapping
    public PageResponse<Product> getProducts(
        @RequestParam(required = false) String name,
        Pageable pageable) {
        
        Page<Product> products = productService.search(name, pageable);
        return PageResponse.of(products);
    }
    
    // Detail view
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        // Response is automatically wrapped in ApiResponse
        return productService.findById(id);
    }
    
    // Create
    @PostMapping
    public Product createProduct(@Valid @RequestBody CreateProductRequest request) {
        // Validation errors automatically generate standard error responses
        return productService.create(request);
    }
    
    // Business exception example
    @PostMapping("/{id}/activate")
    public Product activateProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        
        if (product.isDeleted()) {
            throw new BusinessException(
                "CANNOT_ACTIVATE_DELETED_PRODUCT", 
                "Cannot activate deleted product."
            );
        }
        
        return productService.activate(product);
    }
}
```

### Response Examples

**GET /api/products?page=0&size=5** (HTTP 200)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product 1",
        "price": 10000,
        "status": "ACTIVE"
      }
    ],
    "page": {
      "size": 5,
      "number": 0,
      "totalElements": 100,
      "totalPages": 20
    }
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "requestId": "abc-123-def"
}
```

**POST /api/products (Validation Error)** (HTTP 422)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Input validation failed.",
    "details": [
      "name: Product name is required.",
      "price: Price must be greater than 0."
    ]
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "requestId": "def-456-ghi"
}
```

**Business Exception** (HTTP 400)
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found."
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "requestId": "ghi-789-jkl"
}
```

## 🖥️ Client-side Usage

### Error Handling with HTTP Status Codes

```javascript
// JavaScript/TypeScript example
async function fetchUser(id) {
  try {
    const response = await fetch(`/api/users/${id}`);
    
    if (response.status === 200) {
      // Success
      const result = await response.json();
      return result.data;
    } else if (response.status === 400) {
      // Business logic error
      const error = await response.json();
      throw new BusinessError(error.error.code, error.error.message);
    } else if (response.status === 404) {
      // Resource not found
      throw new NotFoundError('User not found.');
    } else if (response.status === 422) {
      // Validation error
      const error = await response.json();
      throw new ValidationError(error.error.details);
    } else if (response.status === 500) {
      // Server error
      throw new ServerError('Server error occurred.');
    }
  } catch (error) {
    console.error('API call failed:', error);
    throw error;
  }
}
```

### Using Axios Interceptors

```javascript
// Consistent error handling with Axios response interceptor
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        // Business logic error
        showBusinessError(data.error.message);
        break;
      case 404:
        // Resource not found
        showNotFoundError();
        break;
      case 422:
        // Validation error
        showValidationErrors(data.error.details);
        break;
      case 500:
        // Server error
        showServerError();
        break;
    }
    
    return Promise.reject(error);
  }
);
```

## 🔧 Troubleshooting

### When Auto Configuration Doesn't Work

1. **Check JAR file**: Verify dependency is correctly added
2. **Check configuration**: Ensure `web-starter.enabled=false` is not set
3. **Spring Boot version**: Spring Boot 3.5.x or higher recommended

### Excluding Response Wrapping for Specific Endpoints

Currently, only String responses are automatically excluded. Additional exclusions require separate implementation.

### When CORS Configuration Doesn't Work

```yaml
web-starter:
  cors:
    enabled: true
    allowed-origins: "*"  # or specify concrete domains
```

## 📞 Support

If you encounter issues or have improvement suggestions, please submit an issue.

---

Develop Spring Boot applications faster and more consistently with **Web Starter**! 🚀