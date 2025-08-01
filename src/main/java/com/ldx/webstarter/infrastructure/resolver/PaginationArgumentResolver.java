package com.ldx.webstarter.infrastructure.resolver;

import com.ldx.webstarter.infrastructure.properties.PaginationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

/**
 * 페이지네이션 인수 리졸버.
 * 
 * <p>Pageable 타입의 파라미터에 대해 자동으로 제한을 적용합니다.
 * 최대 크기, 최대 요소 수 등의 제한을 강제하여 서버 부하를 방지합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@ConditionalOnClass(Pageable.class)
public class PaginationArgumentResolver implements HandlerMethodArgumentResolver {
    
    private final PaginationProperties paginationProperties;
    
    public PaginationArgumentResolver(PaginationProperties paginationProperties) {
        this.paginationProperties = paginationProperties;
    }
    
    /**
     * 해당 파라미터가 지원되는지 확인합니다.
     * 
     * @param parameter 메서드 파라미터
     * @return Pageable 타입이고 페이지네이션이 활성화된 경우 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return paginationProperties.isEnabled() 
                && Pageable.class.isAssignableFrom(parameter.getParameterType());
    }
    
    /**
     * 페이지네이션 파라미터를 리졸브하고 제한을 적용합니다.
     * 
     * @param parameter 메서드 파라미터
     * @param mavContainer 모델과 뷰 컨테이너
     * @param webRequest 웹 요청
     * @param binderFactory 데이터 바인더 팩토리
     * @return 제한이 적용된 Pageable 객체
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        
        // 요청 파라미터 추출
        int page = getIntParameter(webRequest, paginationProperties.getPageParameter(), 0);
        int size = getIntParameter(webRequest, paginationProperties.getSizeParameter(), paginationProperties.getDefaultSize());
        String sortParam = webRequest.getParameter(paginationProperties.getSortParameter());
        
        // 제한 적용
        size = Math.min(size, paginationProperties.getMaxSize());
        page = Math.max(page, 0);
        
        // 최대 요소 수 제한 검사
        long maxOffset = (long) page * size;
        if (maxOffset >= paginationProperties.getMaxElements()) {
            // 최대 요소 수를 초과하지 않는 페이지로 조정
            page = (int) (paginationProperties.getMaxElements() / size);
        }
        
        // 정렬 처리
        Sort sort = parseSort(sortParam);
        
        return PageRequest.of(page, size, sort);
    }
    
    /**
     * 정수형 파라미터를 안전하게 추출합니다.
     * 
     * @param webRequest 웹 요청
     * @param paramName 파라미터 이름
     * @param defaultValue 기본값
     * @return 파라미터 값 또는 기본값
     */
    private int getIntParameter(NativeWebRequest webRequest, String paramName, int defaultValue) {
        String value = webRequest.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 정렬 파라미터를 파싱합니다.
     * 
     * @param sortParam 정렬 파라미터 (예: "name,asc" 또는 "id,desc")
     * @return Sort 객체
     */
    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return Sort.unsorted();
        }
        
        String[] parts = sortParam.split(",");
        if (parts.length < 1) {
            return Sort.unsorted();
        }
        
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.ASC;
        
        if (parts.length > 1) {
            String directionStr = parts[1].trim().toLowerCase();
            if ("desc".equals(directionStr)) {
                direction = Sort.Direction.DESC;
            }
        }
        
        return Sort.by(direction, property);
    }
}