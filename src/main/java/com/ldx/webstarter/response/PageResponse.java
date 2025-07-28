package com.ldx.webstarter.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 클래스.
 * 
 * <p>페이지네이션된 데이터와 페이징 정보를 포함하는 응답을 제공합니다.
 * Spring Data의 Page 객체를 표준 형식으로 변환합니다.
 * 
 * @param <T> 응답 데이터의 타입
 * @author web-starter
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    
    private final List<T> content;
    private final PageInfo page;
    
    private PageResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }
    
    /**
     * Spring Data Page 객체로부터 PageResponse를 생성합니다.
     * 
     * @param <T> 데이터 타입
     * @param page Spring Data Page 객체
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageInfo pageInfo = PageInfo.of(
            page.getSize(),
            page.getNumber(),
            page.getTotalElements(),
            page.getTotalPages()
        );
        return new PageResponse<>(page.getContent(), pageInfo);
    }
    
    /**
     * 직접 페이징 정보를 지정하여 PageResponse를 생성합니다.
     * 
     * @param <T> 데이터 타입
     * @param content 응답 데이터 목록
     * @param size 페이지 크기
     * @param number 현재 페이지 번호 (0부터 시작)
     * @param totalElements 전체 요소 수
     * @param totalPages 전체 페이지 수
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(List<T> content, int size, int number, long totalElements, int totalPages) {
        PageInfo pageInfo = PageInfo.of(size, number, totalElements, totalPages);
        return new PageResponse<>(content, pageInfo);
    }
    
    public List<T> getContent() {
        return content;
    }
    
    public PageInfo getPage() {
        return page;
    }
    
    /**
     * 페이징 정보 클래스.
     */
    public static class PageInfo {
        private final int size;
        private final int number;
        private final long totalElements;
        private final int totalPages;
        
        private PageInfo(int size, int number, long totalElements, int totalPages) {
            this.size = size;
            this.number = number;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
        
        /**
         * 페이징 정보를 생성합니다.
         * 
         * @param size 페이지 크기
         * @param number 현재 페이지 번호 (0부터 시작)
         * @param totalElements 전체 요소 수
         * @param totalPages 전체 페이지 수
         * @return PageInfo 객체
         */
        public static PageInfo of(int size, int number, long totalElements, int totalPages) {
            return new PageInfo(size, number, totalElements, totalPages);
        }
        
        public int getSize() {
            return size;
        }
        
        public int getNumber() {
            return number;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
    }
}