package com.ldx.webstarter.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PageResponse 테스트.
 * 
 * @author web-starter
 * @since 1.0.0
 */
class PageResponseTest {
    
    @Test
    @DisplayName("Spring Data Page로부터 PageResponse를 생성한다")
    void createFromSpringDataPage() {
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 23);
        
        PageResponse<String> response = PageResponse.of(page);
        
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getPage().getSize()).isEqualTo(10);
        assertThat(response.getPage().getNumber()).isEqualTo(0);
        assertThat(response.getPage().getTotalElements()).isEqualTo(23);
        assertThat(response.getPage().getTotalPages()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("직접 페이징 정보를 지정하여 PageResponse를 생성한다")
    void createWithDirectPageInfo() {
        List<String> content = List.of("item1", "item2");
        int size = 20;
        int number = 1;
        long totalElements = 50;
        int totalPages = 3;
        
        PageResponse<String> response = PageResponse.of(content, size, number, totalElements, totalPages);
        
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getPage().getSize()).isEqualTo(size);
        assertThat(response.getPage().getNumber()).isEqualTo(number);
        assertThat(response.getPage().getTotalElements()).isEqualTo(totalElements);
        assertThat(response.getPage().getTotalPages()).isEqualTo(totalPages);
    }
    
    @Test
    @DisplayName("PageInfo를 직접 생성한다")
    void createPageInfo() {
        int size = 15;
        int number = 2;
        long totalElements = 100;
        int totalPages = 7;
        
        PageResponse.PageInfo pageInfo = PageResponse.PageInfo.of(size, number, totalElements, totalPages);
        
        assertThat(pageInfo.getSize()).isEqualTo(size);
        assertThat(pageInfo.getNumber()).isEqualTo(number);
        assertThat(pageInfo.getTotalElements()).isEqualTo(totalElements);
        assertThat(pageInfo.getTotalPages()).isEqualTo(totalPages);
    }
}