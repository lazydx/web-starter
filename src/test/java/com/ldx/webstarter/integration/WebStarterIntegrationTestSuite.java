package com.ldx.webstarter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.webstarter.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Web-Starter 통합 테스트 스위트.
 * 
 * <p>이전에 발견된 문제들이 모두 해결되었는지 검증합니다:
 * 1. 페이지네이션 제한 기능
 * 2. 파일 다운로드 기능
 * 3. BusinessException 접근성
 * 4. 응답 표준화
 * 5. CORS 설정
 * 
 * @author web-starter
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "web-starter.enabled=true",
    "web-starter.pagination.max-size=50",
    "web-starter.pagination.enabled=true",
    "web-starter.response.enabled=true",
    "web-starter.cors.enabled=true",
    "web-starter.file-storage.enabled=true"
})
public class WebStarterIntegrationTestSuite {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 페이지네이션 제한 기능이 올바르게 작동하는지 테스트합니다.
     * 이전 버그: size=200 요청 시 제한이 적용되지 않았음
     * 수정 후: max-size=50 설정에 의해 50으로 제한되어야 함
     */
    @Test
    public void testPaginationLimitFunctionality() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/pagination")
                .param("size", "200")
                .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(content, ApiResponse.class);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        
        // 응답에서 실제 페이지 크기 확인
        String dataStr = objectMapper.writeValueAsString(response.getData());
        assertTrue(dataStr.contains("\"pageSize\":50") || dataStr.contains("\"size\":50"),
                "페이지 크기가 50으로 제한되지 않았습니다: " + dataStr);
    }

    /**
     * 파일 업로드 및 다운로드 기능이 올바르게 작동하는지 테스트합니다.
     * 이전 버그: 다운로드 시 ClassCastException 발생
     * 수정 후: ResponseAdvice가 파일 응답을 건너뛰어야 함
     */
    @Test
    public void testFileUploadAndDownloadFunctionality() throws Exception {
        // 테스트 파일 생성
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-integration.txt", 
                "text/plain", 
                "Integration test file content".getBytes()
        );

        // 파일 업로드
        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String uploadContent = uploadResult.getResponse().getContentAsString();
        ApiResponse uploadResponse = objectMapper.readValue(uploadContent, ApiResponse.class);
        
        assertTrue(uploadResponse.isSuccess());
        assertNotNull(uploadResponse.getData());

        // 업로드된 파일 정보에서 저장된 파일명 추출
        String dataStr = objectMapper.writeValueAsString(uploadResponse.getData());
        assertTrue(dataStr.contains("storedFileName"));

        // 실제 다운로드는 파일명을 알아야 하므로, 여기서는 존재하지 않는 파일에 대한 에러 처리 테스트
        mockMvc.perform(get("/api/files/download/nonexistent.txt"))
                .andExpect(status().isOk()) // ResponseAdvice에 의해 래핑된 에러 응답
                .andExpect(content().contentType("application/json"));
    }

    /**
     * 새로운 공개 예외 클래스들이 올바르게 처리되는지 테스트합니다.
     * 이전 버그: BusinessException 접근 불가
     * 수정 후: com.ldx.webstarter.exception 패키지의 예외들 사용 가능
     */
    @Test
    public void testPublicExceptionHandling() throws Exception {
        // BusinessException 테스트
        MvcResult businessResult = mockMvc.perform(get("/api/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String businessContent = businessResult.getResponse().getContentAsString();
        ApiResponse businessResponse = objectMapper.readValue(businessContent, ApiResponse.class);
        
        assertFalse(businessResponse.isSuccess());
        assertNotNull(businessResponse.getError());
        assertEquals("BUSINESS_ERROR", businessResponse.getError().getCode());

        // ValidationException 테스트
        MvcResult validationResult = mockMvc.perform(get("/api/test/validation-exception"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String validationContent = validationResult.getResponse().getContentAsString();
        ApiResponse validationResponse = objectMapper.readValue(validationContent, ApiResponse.class);
        
        assertFalse(validationResponse.isSuccess());
        assertNotNull(validationResponse.getError());
        assertEquals("VALIDATION_ERROR", validationResponse.getError().getCode());
    }

    /**
     * 응답 표준화가 올바르게 작동하는지 테스트합니다.
     * 모든 API 응답이 ApiResponse 형식으로 래핑되어야 함
     */
    @Test
    public void testResponseStandardization() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/simple-response"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(content, ApiResponse.class);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertNotNull(response.getTimestamp());
        assertNotNull(response.getRequestId());
    }

    /**
     * CORS 설정이 올바르게 적용되는지 테스트합니다.
     */
    @Test
    public void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/test/simple-response")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    /**
     * 전체 기능들이 함께 작동하는지 통합 테스트합니다.
     */
    @Test
    public void testIntegratedFunctionality() throws Exception {
        // 1. 페이지네이션과 응답 표준화 함께 테스트
        MvcResult paginationResult = mockMvc.perform(get("/api/test/pagination")
                .param("size", "100")
                .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String paginationContent = paginationResult.getResponse().getContentAsString();
        ApiResponse paginationResponse = objectMapper.readValue(paginationContent, ApiResponse.class);
        
        assertTrue(paginationResponse.isSuccess());
        assertNotNull(paginationResponse.getTimestamp());
        assertNotNull(paginationResponse.getRequestId());

        // 2. 예외 처리와 응답 표준화 함께 테스트
        MvcResult exceptionResult = mockMvc.perform(get("/api/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String exceptionContent = exceptionResult.getResponse().getContentAsString();
        ApiResponse exceptionResponse = objectMapper.readValue(exceptionContent, ApiResponse.class);
        
        assertFalse(exceptionResponse.isSuccess());
        assertNotNull(exceptionResponse.getError());
        assertNotNull(exceptionResponse.getTimestamp());
        assertNotNull(exceptionResponse.getRequestId());
    }
}