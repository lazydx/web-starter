package com.ldx.webstarter.improvement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * String 응답 자동 래핑 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StringResponseTest.TestController.class)
class StringResponseTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("단순 문자열 응답이 ApiResponse로 자동 래핑된다")
    void stringResponseShouldBeWrapped() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/string", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("\"data\":\"Hello, Web-Starter!\"");
        assertThat(response.getBody()).contains("\"timestamp\":");
        assertThat(response.getBody()).contains("\"requestId\":");
    }
    
    @Test
    @DisplayName("숫자 응답도 ApiResponse로 자동 래핑된다")
    void numberResponseShouldBeWrapped() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/number", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("\"data\":42");
    }
    
    @RestController
    static class TestController {
        
        @GetMapping("/test/string")
        public String getString() {
            return "Hello, Web-Starter!";
        }
        
        @GetMapping("/test/number")
        public Integer getNumber() {
            return 42;
        }
    }
}