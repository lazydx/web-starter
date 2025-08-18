package com.ldx.webstarter;

import com.ldx.webstarter.exception.BusinessException;
import com.ldx.webstarter.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 통합 테스트용 컨트롤러.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/pagination")
    public Page<Map<String, Object>> testPagination(Pageable pageable) {
        // 테스트용 데이터 생성
        List<Map<String, Object>> content = List.of(
                Map.of("id", 1, "name", "Test Item 1"),
                Map.of("id", 2, "name", "Test Item 2"),
                Map.of("id", 3, "name", "Test Item 3")
        );
        
        return new PageImpl<>(content, pageable, 100);
    }

    @GetMapping("/simple-response")
    public Map<String, Object> testSimpleResponse() {
        return Map.of(
                "message", "Simple response test",
                "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/business-exception")
    public String testBusinessException() {
        throw new BusinessException("BUSINESS_ERROR", "Test business exception for integration test");
    }

    @GetMapping("/validation-exception")
    public String testValidationException() {
        throw new ValidationException("VALIDATION_ERROR", "Test validation exception for integration test");
    }
}