# 🏗️ AI를 위한 Spring Boot Starter 표준 아키텍처 가이드라인

## 🎯 **핵심 원칙 (AI 필수 암기사항)**

### **절대 규칙 (NEVER BREAK)**
1. **Zero Configuration**: 아무 설정 없어도 오류 없이 실행되어야 함
2. **Component Scan 독립성**: `@SpringBootApplication(scanBasePackages=...)` 추가 필요 없어야 함  
3. **부모 우선**: 부모 프로젝트 Bean 존재하면 스타터 Bean 등록 안함
4. **단일 조건**: 복잡한 조건 조합 금지, 최대한 단순하게
5. **의존성 격리**: 스타터 내부 의존성이 부모 프로젝트로 전파되면 안됨

---

## 📋 **AI 구현 단계별 체크리스트**

### **Phase 1: 프로젝트 초기 설정**

#### **1.1 프로젝트 구조 생성**
```bash
# 필수 디렉토리 생성
{domain}-starter/
├── src/main/java/com/ldx/{domain}starter/
│   ├── infrastructure/
│   │   ├── autoconfigure/          # AutoConfiguration 클래스들  
│   │   ├── properties/             # Properties 클래스들
│   │   └── exception/              # 예외 처리 클래스들
│   ├── {feature}/                  # 기능별 패키지 (response, file 등)
│   └── WebStarterApplication.java  # 테스트용 (운영 제외)
├── src/main/resources/
│   ├── META-INF/
│   │   └── spring.factories        # AutoConfiguration 등록
│   └── application.yml              # 기본 설정값
└── build.gradle or pom.xml
```

#### **✅ Phase 1 검증:**
- [ ] 패키지명이 `com.ldx.{domain}starter` 형태인가?
- [ ] `infrastructure` 패키지가 있는가?
- [ ] `META-INF/spring.factories` 파일이 있는가?

---

### **Phase 2: Properties 클래스 구현**

#### **2.1 루트 Properties 템플릿**
```java
@ConfigurationProperties(prefix = "{domain}-starter")
@Validated
public class {Domain}StarterProperties {
    
    /**
     * 전체 스타터 활성화 여부
     * 기본값: true (Zero Configuration)
     */
    private boolean enabled = true;
    
    /**
     * 아키텍처 모드
     * traditional: 모든 레이어에서 자유 사용
     * hexagonal: Domain Layer에서 사용 금지
     */
    private Mode mode = Mode.TRADITIONAL;
    
    /**
     * 기능별 토글 (구체적인 기능명 사용)
     */
    @NestedConfigurationProperty
    private FeatureToggle response = new FeatureToggle(true);      // API 응답 표준화
    
    @NestedConfigurationProperty  
    private FeatureToggle exception = new FeatureToggle(true);     // 전역 예외 처리
    
    @NestedConfigurationProperty
    private FeatureToggle cors = new FeatureToggle(true);          // CORS 설정
    
    @NestedConfigurationProperty
    private FeatureToggle file = new FeatureToggle(false);         // 파일 처리 (기본 OFF)
    
    @NestedConfigurationProperty
    private FeatureToggle debug = new FeatureToggle(false);        // 디버그 기능 (기본 OFF)
    
    // 편의 메서드들 (조건 로직을 Properties 내부로 이동)
    public boolean isResponseEnabled() { return enabled && response.isEnabled(); }
    public boolean isExceptionEnabled() { return enabled && exception.isEnabled(); }
    public boolean isCorsEnabled() { return enabled && cors.isEnabled(); }
    public boolean isFileEnabled() { return enabled && file.isEnabled(); }
    public boolean isDebugEnabled() { return enabled && debug.isEnabled(); }
    
    public enum Mode {
        TRADITIONAL,    // 전통적 MVC - 모든 레이어 자유 사용
        HEXAGONAL      // 헥사고날 - Domain Layer 제한
    }
    
    public static class FeatureToggle {
        private boolean enabled;
        
        public FeatureToggle(boolean defaultEnabled) {
            this.enabled = defaultEnabled;
        }
        
        // getter/setter...
    }
    
    // getter/setter 메서드들...
}
```

#### **✅ Phase 2 검증:**
- [ ] `@ConfigurationProperties(prefix = "{domain}-starter")` 설정되었는가?
- [ ] 모든 필드에 기본값이 있는가? (Zero Configuration)
- [ ] `is{Feature}Enabled()` 편의 메서드가 있는가?
- [ ] Mode enum에 TRADITIONAL, HEXAGONAL이 있는가?

---

### **Phase 3: AutoConfiguration 구현**

#### **3.1 메인 AutoConfiguration 템플릿**
```java
/**
 * {Domain} Starter 메인 자동 설정
 * 
 * 핵심 원칙:
 * 1. 단일 조건만 사용 (@ConditionalOnProperty 1개)
 * 2. 모든 Bean은 @ConditionalOnMissingBean (부모 우선)
 * 3. 조건 로직은 Properties에서 처리
 * 4. Component 어노테이션 사용 금지
 */
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "{domain}-starter", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true  // Zero Configuration
)
@EnableConfigurationProperties({Domain}StarterProperties.class)
public class {Domain}StarterAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger({Domain}StarterAutoConfiguration.class);
    
    /**
     * 예외 처리 Bean (GlobalExceptionHandler)
     */
    @Bean
    @ConditionalOnMissingBean(name = "globalExceptionHandler")  // 부모 우선
    public GlobalExceptionHandler globalExceptionHandler({Domain}StarterProperties props) {
        
        if (!props.isExceptionEnabled()) {
            logger.debug("{} exception handling disabled", "{domain}-starter");
            return null;  // Bean 등록 안함
        }
        
        // 헥사고날/전통적 모드에 따른 분기
        if (props.getMode() == {Domain}StarterProperties.Mode.HEXAGONAL) {
            logger.info("{} exception handler initialized in HEXAGONAL mode", "{domain}-starter");
            return new HexagonalGlobalExceptionHandler(props);
        } else {
            logger.info("{} exception handler initialized in TRADITIONAL mode", "{domain}-starter");  
            return new TraditionalGlobalExceptionHandler(props);
        }
    }
    
    /**
     * API 응답 표준화 Bean
     */
    @Bean
    @ConditionalOnMissingBean(ResponseAdvice.class)
    public ResponseAdvice responseAdvice({Domain}StarterProperties props) {
        
        if (!props.isResponseEnabled()) {
            return null;
        }
        
        return new ResponseAdvice(props);
    }
    
    /**
     * 파일 처리 Controller (Component Scan 회피)
     */
    @Bean  
    @ConditionalOnMissingBean(FileUploadController.class)
    public FileUploadController fileUploadController({Domain}StarterProperties props) {
        
        if (!props.isFileEnabled()) {
            return null;
        }
        
        // 명시적 Bean 등록 (Component Scan 불필요)
        return new FileUploadController(props);
    }
}
```

#### **✅ Phase 3 검증:**
- [ ] `@ConditionalOnProperty` 조건이 1개뿐인가?
- [ ] `matchIfMissing = true`로 설정되었는가? 
- [ ] 모든 Bean에 `@ConditionalOnMissingBean`이 있는가?
- [ ] Bean 생성 전 `props.is{Feature}Enabled()` 체크하는가?
- [ ] 헥사고날/전통적 모드 분기 로직이 있는가?
- [ ] `@Component`, `@Service`, `@Controller` 어노테이션 사용하지 않았는가?

---

### **Phase 4: Component Scan 회피 구현**

#### **4.1 Controller/Service 올바른 구현**
```java
/**
 * ❌ 금지: @Controller 어노테이션 사용
 * ✅ 권장: 일반 클래스로 구현
 */
public class FileUploadController {  // @RestController 없음!
    
    private final FileStorageService fileStorageService;
    
    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    @PostMapping("/api/{domain}/files/upload")
    public ResponseEntity<ApiResponse<FileMetadata>> upload(@RequestParam("file") MultipartFile file) {
        FileMetadata metadata = fileStorageService.store(file);
        return ResponseEntity.ok(ApiResponse.success(metadata));
    }
}

/**
 * ❌ 금지: @Service 어노테이션 사용  
 * ✅ 권장: 일반 클래스로 구현
 */
public class FileStorageService {  // @Service 없음!
    
    private final FileStorageProperties properties;
    
    public FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }
    
    public FileMetadata store(MultipartFile file) {
        // 구현...
    }
}
```

#### **4.2 AutoConfiguration에서 Bean 등록**
```java
@AutoConfiguration  
public class FileAutoConfiguration {
    
    /**
     * FileStorageService Bean 등록 (Component Scan 회피)
     */
    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService(FileStorageProperties props) {
        return new FileStorageService(props);  // 명시적 생성
    }
    
    /**
     * FileUploadController Bean 등록 (Component Scan 회피)
     */
    @Bean
    @ConditionalOnMissingBean  
    public FileUploadController fileUploadController(FileStorageService service) {
        return new FileUploadController(service);  // 명시적 생성
    }
}
```

#### **✅ Phase 4 검증:**
- [ ] Controller 클래스에 `@RestController` 없는가?
- [ ] Service 클래스에 `@Service` 없는가?
- [ ] Component 클래스에 `@Component` 없는가?
- [ ] AutoConfiguration에서 모든 Bean을 명시적으로 등록하는가?
- [ ] 사용자가 `scanBasePackages` 추가 없이도 동작하는가?

---

### **Phase 5: 헥사고날/전통적 모드 구현**

#### **5.1 모드별 Bean 구현**
```java
/**
 * 전통적 모드 - 모든 레이어에서 자유 사용
 */
public class TraditionalGlobalExceptionHandler extends GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.info("Traditional mode: Business exception - {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of(e.getCode(), e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }
}

/**
 * 헥사고날 모드 - Domain Layer 제한
 */
public class HexagonalGlobalExceptionHandler extends GlobalExceptionHandler {
    
    @ExceptionHandler(ApplicationBusinessException.class)  // Application Layer 예외만
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationBusinessException e) {
        logger.warn("Hexagonal mode: Application exception - {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.of(e.getCode(), e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }
    
    // Domain 예외는 처리하지 않음 (의존성 역전 위반 방지)
}
```

#### **5.2 아키텍처 검증 (선택적)**
```java
/**
 * 헥사고날 모드일 때 Domain Layer 검증
 */
@Component
@ConditionalOnProperty(prefix = "{domain}-starter.architecture", name = "enforce-boundaries", havingValue = "true")
public class HexagonalArchitectureValidator {
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateHexagonalBoundaries() {
        
        ArchRule domainPurityRule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.ldx.{domain}starter..")
            .because("Domain layer cannot use {domain}-starter in hexagonal mode");
            
        domainPurityRule.check(JavaClasses.of(""));
    }
}
```

#### **✅ Phase 5 검증:**
- [ ] Traditional/Hexagonal 각각 다른 Bean 클래스가 있는가?
- [ ] AutoConfiguration에서 `if-else` 분기로 Bean 생성하는가?
- [ ] 헥사고날 모드에서 Domain 예외를 처리하지 않는가?
- [ ] 아키텍처 검증이 선택적으로 활성화되는가?

---

### **Phase 6: 설정 파일 구성**

#### **6.1 META-INF/spring.factories**
```properties
# Auto Configuration
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.ldx.{domain}starter.infrastructure.autoconfigure.{Domain}StarterAutoConfiguration
```

#### **6.2 기본 application.yml**
```yaml
# {domain}-starter 기본 설정
{domain}-starter:
  enabled: true
  mode: traditional
  
  # 기능별 설정 (구체적인 기능명 사용)
  response:
    enabled: true
  exception:
    enabled: true  
  cors:
    enabled: true
  file:
    enabled: false
  debug:
    enabled: false

# 사용자 프로젝트에서 오버라이드 가능한 설정들
logging:
  level:
    com.ldx.{domain}starter: INFO
```

#### **✅ Phase 6 검증:**
- [ ] `spring.factories`에 AutoConfiguration이 등록되었는가?
- [ ] 기본 설정에서 모든 기능이 명시적으로 정의되었는가?
- [ ] 사용자가 오버라이드할 수 있는 설정이 있는가?

---

### **Phase 7: 사용자 통합 검증**

#### **7.1 사용자 프로젝트 테스트**
```java
/**
 * 사용자 프로젝트 예시 - 아무것도 추가 안해도 동작해야 함
 */
@SpringBootApplication  // scanBasePackages 없음!
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}

@RestController
public class UserController {
    
    @Autowired
    private FileStorageService fileStorage;  // 자동 주입됨
    
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) {
        // {domain}-starter의 FileStorageService 사용
        return fileStorage.store(file);
    }
    
    @GetMapping("/users") 
    public List<User> getUsers() {
        // 예외 발생 시 GlobalExceptionHandler가 자동 처리
        return userService.getUsers();
    }
}
```

#### **7.2 설정 오버라이드 테스트**
```yaml
# 사용자 프로젝트 application.yml
{domain}-starter:
  enabled: true
  mode: hexagonal     # 헥사고날 모드 변경
  file:
    enabled: true     # 파일 기능 활성화
  debug:
    enabled: true     # 디버그 활성화
```

#### **✅ Phase 7 검증:**
- [ ] 의존성만 추가해도 오류 없이 실행되는가?
- [ ] `@SpringBootApplication`에 추가 설정 없어도 되는가?
- [ ] 스타터 Bean들이 자동 주입되는가?
- [ ] application.yml 설정이 정상 적용되는가?
- [ ] 헥사고날/전통적 모드 전환이 동작하는가?

---

## 🚨 **AI 필수 확인사항 (배포 전)**

### **최종 체크리스트**
```bash
# 1. Zero Configuration 테스트
./gradlew clean build
java -jar build/libs/{domain}-starter-test.jar
# → 오류 없이 실행되어야 함

# 2. Component Scan 독립성 테스트  
# 사용자 프로젝트에서 scanBasePackages 없이 테스트
# → 모든 Bean이 정상 주입되어야 함

# 3. Bean 충돌 테스트
# 부모 프로젝트에 동일한 Bean 등록 후 테스트
# → 부모 Bean이 우선 사용되어야 함

# 4. 모드 전환 테스트
# traditional ↔ hexagonal 모드 전환 후 동작 확인
# → 각각 다른 Bean이 생성되어야 함

# 5. 의존성 격리 테스트
./gradlew dependencies
# → 스타터 내부 의존성이 부모로 전파되지 않아야 함
```

---

## 📚 **코드 템플릿 요약**

### **필수 파일 체크리스트**
- [ ] `{Domain}StarterProperties.java` - 루트 Properties
- [ ] `{Domain}StarterAutoConfiguration.java` - 메인 AutoConfiguration  
- [ ] `GlobalExceptionHandler.java` - 예외 처리
- [ ] `META-INF/spring.factories` - 자동 등록
- [ ] `application.yml` - 기본 설정

### **네이밍 규칙**
- **패키지**: `com.ldx.{domain}starter`
- **Properties**: `{Domain}StarterProperties`  
- **AutoConfiguration**: `{Domain}StarterAutoConfiguration`
- **설정 prefix**: `{domain}-starter`

---

## 🎯 **실제 사용 예시**

### **web-starter 적용 예시**
```yaml
# 사용자 프로젝트 application.yml
web-starter:
  enabled: true
  mode: hexagonal
  
  response:
    enabled: true
  exception:
    enabled: true
  cors:
    enabled: true
  file:
    enabled: true
  debug:
    enabled: false
```

```java
// 사용자 프로젝트 - 헥사고날 아키텍처
@SpringBootApplication
public class MyHexagonalApp {
    public static void main(String[] args) {
        SpringApplication.run(MyHexagonalApp.class, args);
    }
}

// Domain Layer - web-starter 사용 불가
@DomainService
public class OrderDomainService {
    public void processOrder(Order order) {
        // 순수 도메인 로직만
        // web-starter 사용 시 런타임 검증 오류
    }
}

// Application Layer - web-starter 사용 가능
@ApplicationService
public class OrderApplicationService {
    public void handleOrder(OrderCommand command) {
        try {
            orderDomainService.processOrder(command.toOrder());
        } catch (OrderValidationException e) {
            // ApplicationBusinessException으로 변환
            throw new ApplicationBusinessException("ORDER_INVALID", e.getMessage());
        }
    }
}

// Adapter Layer - web-starter 자유 사용
@RestController
public class OrderController {
    
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        // ApplicationBusinessException 발생 시
        // → HexagonalGlobalExceptionHandler가 자동 처리
        OrderDto order = orderApplicationService.handleOrder(request.toCommand());
        
        // ResponseAdvice가 자동으로 ApiResponse 형태로 변환
        return ResponseEntity.ok(order);
    }
}
```

**이 가이드라인을 단계별로 따라하면 표준적인 Spring Boot Starter를 구현할 수 있습니다!**