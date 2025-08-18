package com.ldx.webstarter.improvement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 개선된 예외 처리 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ImprovedExceptionHandlingTest.TestController.class)
class ImprovedExceptionHandlingTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("IllegalArgumentException이 400 Bad Request로 처리된다")
    void illegalArgumentExceptionShouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/illegal-argument", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"success\":false");
        assertThat(response.getBody()).contains("\"code\":\"BAD_REQUEST\"");
        assertThat(response.getBody()).contains("\"message\":\"잘못된 인수입니다\"");
    }
    
    @Test
    @DisplayName("NullPointerException이 400 Bad Request로 처리된다")
    void nullPointerExceptionShouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/null-pointer", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"success\":false");
        assertThat(response.getBody()).contains("\"code\":\"BAD_REQUEST\"");
        assertThat(response.getBody()).contains("\"message\":\"필수 값이 누락되었습니다\"");
    }
    
    @Test
    @DisplayName("IllegalStateException이 409 Conflict로 처리된다")
    void illegalStateExceptionShouldReturn409() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/illegal-state", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("\"success\":false");
        assertThat(response.getBody()).contains("\"code\":\"CONFLICT\"");
        assertThat(response.getBody()).contains("\"message\":\"잘못된 상태입니다\"");
    }
    
    @Test
    @DisplayName("범위별 ID 값에 따른 다양한 예외 처리")
    void variousExceptionsByIdRange() {
        // IllegalArgumentException (1-10)
        ResponseEntity<String> response1 = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/user/5", String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // NullPointerException (11-20)  
        ResponseEntity<String> response2 = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/user/15", String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // IllegalStateException (21-30)
        ResponseEntity<String> response3 = restTemplate.getForEntity(
            "http://localhost:" + port + "/test/user/25", String.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
    
    @RestController
    static class TestController {
        
        @GetMapping("/test/illegal-argument")
        public String throwIllegalArgument() {
            throw new IllegalArgumentException("잘못된 인수입니다");
        }
        
        @GetMapping("/test/null-pointer")
        public String throwNullPointer() {
            throw new NullPointerException();
        }
        
        @GetMapping("/test/illegal-state")
        public String throwIllegalState() {
            throw new IllegalStateException("잘못된 상태입니다");
        }
        
        @GetMapping("/test/user/{id}")
        public String getUser(@PathVariable Long id) {
            if (id >= 1 && id <= 10) {
                throw new IllegalArgumentException("ID 범위가 잘못되었습니다: " + id);
            } else if (id >= 11 && id <= 20) {
                throw new NullPointerException();
            } else if (id >= 21 && id <= 30) {
                throw new IllegalStateException("사용자 상태가 올바르지 않습니다");
            }
            return "User: " + id;
        }
    }
}