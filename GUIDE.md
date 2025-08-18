# Web Starter Guide

## Project Application Guide

To integrate Web Starter into your Spring Boot application, follow these steps:

1. Add dependency to your project
2. Configure Web Starter properties (optional)
3. Use standardized response formats and exception handling
4. Leverage pagination, file management, and CORS features

## Add Dependency

### Maven
```xml
<dependency>
    <groupId>io.github.lazydx</groupId>
    <artifactId>web-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.lazydx:web-starter:1.1.0'
```

## Configuration

### Basic Configuration
Add the following to your `application.yml`:

```yaml
web-starter:
  enabled: true
  
  # Response standardization
  response:
    enabled: true
    wrap-response: true
    include-request-id: true
    include-timestamp: true
  
  # CORS configuration
  cors:
    enabled: true
    allowed-origins: "*"
    allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
    allowed-headers: "*"
    allow-credentials: false
    max-age: 3600
  
  # Pagination limits
  pagination:
    enabled: true
    default-size: 20
    max-size: 100
    max-elements: 5000
  
  # File storage
  file-storage:
    enabled: true
    upload:
      max-file-size: "10MB"
      max-request-size: "100MB"
      allowed-extensions: ["jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt"]
      enable-virus-scanning: false
    local:
      base-path: "./uploads"
      create-directories: true
  
  # Debug mode (for development)
  debug:
    enabled: false
    log-requests: false
    log-request-body: false
    log-response-body: false
    log-performance-metrics: false
```

### WebStarterConfig Class Example
```java
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.ldx.webstarter.infrastructure.properties.WebStarterProperties;

@Configuration
@EnableConfigurationProperties(WebStarterProperties.class)
public class WebStarterConfig {
    // Configuration is automatically applied
    // No additional setup required
}
```

## Core Features

### 1. Standardized API Responses

All controller responses are automatically wrapped in a consistent format:

#### Success Response
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // Response is automatically wrapped
        return userService.findById(id);
    }
    
    @GetMapping
    public Page<User> getUsers(Pageable pageable) {
        // Pagination is automatically limited
        return userService.findAll(pageable);
    }
}
```

**Response Format:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "uuid-string"
}
```

### 2. Exception Handling

#### Using Business Exceptions
```java
import com.ldx.webstarter.exception.BusinessException;
import com.ldx.webstarter.exception.ValidationException;

@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "USER_NOT_FOUND", 
                "User with id " + id + " not found"
            ));
    }
    
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                "EMAIL_ALREADY_EXISTS", 
                "Email already exists"
            );
        }
        
        return userRepository.save(new User(request));
    }
}
```

#### Validation Exception Example
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        // Validation errors are automatically handled
        return userService.createUser(request);
    }
}

// Request DTO with validation
public class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    // getters and setters
}
```

**Error Response Format:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      "name: Name is required",
      "email: Invalid email format"
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z",
  "requestId": "uuid-string"
}
```

### 3. File Management

#### File Upload Controller
```java
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    private final FileStorageService fileStorageService;
    
    @PostMapping("/upload")
    public FileMetadata uploadFile(@RequestParam("file") MultipartFile file) {
        // File validation is handled automatically
        return fileStorageService.store(file);
    }
    
    @PostMapping("/upload/multiple")
    public List<FileMetadata> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.stream(files)
            .map(fileStorageService::store)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource resource = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }
}
```

#### File Storage Configuration
```yaml
web-starter:
  file-storage:
    enabled: true
    upload:
      max-file-size: "10MB"
      max-request-size: "100MB"
      allowed-extensions: ["jpg", "jpeg", "png", "pdf", "doc", "docx", "txt"]
      allowed-mime-types: 
        - "image/jpeg"
        - "image/png"
        - "application/pdf"
        - "application/msword"
      enable-virus-scanning: true
      temp-dir: "/tmp/uploads"
    download:
      enable-range-requests: true
      cache-max-age: 3600
    local:
      base-path: "./uploads"
      create-directories: true
    # S3 Configuration (optional)
    s3:
      enabled: false
      bucket-name: "my-app-files"
      region: "us-east-1"
      access-key: "${AWS_ACCESS_KEY}"
      secret-key: "${AWS_SECRET_KEY}"
      path-prefix: "uploads/"
```

### 4. Pagination with Limits

#### Controller with Pagination
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public PageResponse<Product> getProducts(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        Pageable pageable) {
        
        // Pagination is automatically limited
        Page<Product> products = productService.search(name, category, pageable);
        return PageResponse.of(products);
    }
}
```

#### Custom Pagination Configuration
```yaml
web-starter:
  pagination:
    enabled: true
    default-size: 10          # Default page size
    max-size: 50             # Maximum page size allowed
    max-elements: 1000       # Maximum total elements
    page-parameter: "page"    # Page parameter name
    size-parameter: "size"    # Size parameter name
    sort-parameter: "sort"    # Sort parameter name
```

**Usage Examples:**
```bash
# Normal request
curl "http://localhost:8080/api/products?page=0&size=20"

# Size automatically limited to max-size
curl "http://localhost:8080/api/products?page=0&size=150"  # Limited to 50
```

### 5. CORS Configuration

#### Environment-specific CORS
```yaml
# application-dev.yml
web-starter:
  cors:
    enabled: true
    allowed-origins: "*"
    allow-credentials: false

---
# application-prod.yml
web-starter:
  cors:
    enabled: true
    allowed-origins: 
      - "https://myapp.com"
      - "https://admin.myapp.com"
    allowed-methods: "GET,POST,PUT,DELETE"
    allowed-headers: "Content-Type,Authorization"
    allow-credentials: true
    max-age: 3600
```

### 6. Debug Mode

#### Development Debug Configuration
```yaml
web-starter:
  debug:
    enabled: true
    log-requests: true
    log-request-body: true
    log-response-body: true
    log-performance-metrics: true
    log-detailed-exceptions: true
    log-level: "DEBUG"
    max-request-body-log-size: 2048
    max-response-body-log-size: 2048
```

This will provide detailed logging including:
- HTTP request/response details
- Performance metrics and timing
- Slow request detection (>1s warnings, >5s errors)
- Memory usage statistics

## Advanced Usage

### Custom Exception Handling
```java
@Component
public class CustomExceptionHandler {
    
    @EventListener
    public void handleBusinessException(BusinessException ex) {
        // Custom business logic for specific exceptions
        if ("PAYMENT_FAILED".equals(ex.getCode())) {
            // Send notification, log to external system, etc.
        }
    }
}
```

### File Storage Service Injection
```java
@Service
public class DocumentService {
    
    private final FileStorageService fileStorageService;
    private final FileValidationService fileValidationService;
    
    public DocumentMetadata processDocument(MultipartFile file) {
        // Custom validation
        fileValidationService.validate(file);
        
        // Store with custom directory
        FileMetadata metadata = fileStorageService.store(file, "documents");
        
        // Additional processing...
        return new DocumentMetadata(metadata);
    }
}
```

### Conditional Feature Enabling
```yaml
web-starter:
  enabled: true
  response:
    enabled: false      # Disable response wrapping
  cors:
    enabled: false      # Disable CORS
  pagination:
    enabled: true       # Keep pagination limits
  file-storage:
    enabled: false      # Disable file features
```

## Complete Example

### Spring Boot Application
```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### Product Controller Example
```java
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public PageResponse<Product> getProducts(
        @RequestParam(required = false) String name,
        Pageable pageable) {
        
        Page<Product> products = productService.search(name, pageable);
        return PageResponse.of(products);
    }
    
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable @Min(1) Long id) {
        return productService.findById(id);
    }
    
    @PostMapping
    public Product createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }
    
    @PutMapping("/{id}")
    public Product updateProduct(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateProductRequest request) {
        
        return productService.update(id, request);
    }
    
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
    
    @PostMapping("/{id}/activate")
    public Product activateProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        
        if (product.isDeleted()) {
            throw new BusinessException(
                "CANNOT_ACTIVATE_DELETED_PRODUCT", 
                "Cannot activate a deleted product"
            );
        }
        
        return productService.activate(product);
    }
}
```

### Service Layer
```java
@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Page<Product> search(String name, Pageable pageable) {
        if (StringUtils.hasText(name)) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        return productRepository.findAll(pageable);
    }
    
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "PRODUCT_NOT_FOUND", 
                "Product with id " + id + " not found"
            ));
    }
    
    public Product create(CreateProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new BusinessException(
                "PRODUCT_NAME_ALREADY_EXISTS", 
                "Product name already exists"
            );
        }
        
        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .build();
            
        return productRepository.save(product);
    }
}
```

This guide provides comprehensive examples for integrating and using Web Starter in your Spring Boot applications. The library handles common web application patterns automatically while remaining flexible and configurable for specific needs.