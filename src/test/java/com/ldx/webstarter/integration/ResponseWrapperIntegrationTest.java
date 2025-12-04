package com.ldx.webstarter.integration;


import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;


import com.ldx.webstarter.infrastructure.autoconfigure.WebStarterAutoConfiguration;
import com.ldx.webstarter.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

/**
 * Response Wrapper 통합 테스트
 * 
 * API 응답이 자동으로 ApiResponse로 래핑되는지 검증합니다.
 */
@SpringBootTest(classes = {
    WebMvcAutoConfiguration.class,
    WebStarterAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    ResponseWrapperIntegrationTest.TestController.class
})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "web-starter.enabled=true",
    "web-starter.response-toggle.enabled=true",
    "management.endpoints.web.exposure.include=health,prometheus"
})
class ResponseWrapperIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void simpleResponse_shouldBeWrapped() throws Exception {
        // 단순 문자열 응답이 ApiResponse로 래핑되어야 함
        mockMvc.perform(get("/test/simple"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Hello World"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void objectResponse_shouldBeWrapped() throws Exception {
        // 객체 응답이 ApiResponse로 래핑되어야 함
        mockMvc.perform(get("/test/object"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("Test User"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
    
    @Test
    void listResponse_shouldBeWrapped() throws Exception {
        // 리스트 응답이 ApiResponse로 래핑되어야 함
        mockMvc.perform(get("/test/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(3)))
            .andExpect(jsonPath("$.data[0]").value("Item1"))
            .andExpect(jsonPath("$.data[1]").value("Item2"))
            .andExpect(jsonPath("$.data[2]").value("Item3"));
    }
    
    @Test
    void voidResponse_shouldReturnEmptyWrapped() throws Exception {
        // void 응답도 ApiResponse로 래핑되어야 함
        mockMvc.perform(get("/test/void"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void alreadyWrappedResponse_shouldNotBeDoubleWrapped() throws Exception {
        // 이미 ApiResponse인 경우 이중 래핑되지 않아야 함
        mockMvc.perform(get("/test/already-wrapped"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Already wrapped"))
            // 이중 래핑되지 않았는지 확인
            .andExpect(jsonPath("$.data.success").doesNotExist());
    }

    @Test
    void binaryResponse_shouldNotBeWrapped() throws Exception {
        // byte[] 응답은 ApiResponse로 래핑되지 않고, 원본 데이터가 그대로 반환되어야 함
        mockMvc.perform(get("/test/binary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes("binary data".getBytes()))
                // ApiResponse로 래핑되지 않았는지 확인
                .andExpect(content().string(not(containsString("success"))));
    }

    /**
     * 테스트용 컨트롤러
     */
    @RestController
    static class TestController {
        
        @GetMapping("/test/simple")
        public String simpleResponse() {
            return "Hello World";
        }
        
        @GetMapping("/test/object")
        public TestUser objectResponse() {
            return new TestUser(1L, "Test User", "test@example.com");
        }
        
        @GetMapping("/test/list")
        public java.util.List<String> listResponse() {
            return java.util.List.of("Item1", "Item2", "Item3");
        }
        
        @GetMapping("/test/void")
        public void voidResponse() {
            // 아무것도 반환하지 않음
        }
        
        @GetMapping("/test/already-wrapped")
        public ApiResponse<String> alreadyWrappedResponse() {
            return ApiResponse.success("Already wrapped");
        }

        @GetMapping("/test/binary")
        public byte[] binaryResponse() {
            return "binary data".getBytes();
        }
        
        static class TestUser {
            private final Long id;
            private final String name;
            private final String email;
            
            TestUser(Long id, String name, String email) {
                this.id = id;
                this.name = name;
                this.email = email;
            }
            
            public Long getId() { return id; }
            public String getName() { return name; }
            public String getEmail() { return email; }
        }
    }
}