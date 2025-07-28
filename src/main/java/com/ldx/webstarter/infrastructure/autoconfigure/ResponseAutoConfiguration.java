package com.ldx.webstarter.infrastructure.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.webstarter.infrastructure.advice.ResponseAdvice;
import com.ldx.webstarter.infrastructure.properties.ResponseProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 응답 표준화 자동 설정 클래스.
 * 
 * <p>API 응답을 표준 형식으로 통일하는 기능을 자동 설정합니다.
 * ResponseAdvice를 통해 모든 컨트롤러 응답을 ApiResponse로 래핑합니다.
 * 
 * @author web-starter
 * @since 1.0.0
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
    prefix = "web-starter.response",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(ResponseProperties.class)
public class ResponseAutoConfiguration implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseAutoConfiguration.class);
    
    /**
     * 응답 표준화를 위한 ResponseAdvice 빈을 생성합니다.
     * 
     * @param objectMapper JSON 직렬화를 위한 ObjectMapper
     * @return ResponseAdvice 인스턴스
     */
    @Bean
    public ResponseAdvice responseAdvice(ObjectMapper objectMapper) {
        return new ResponseAdvice(objectMapper);
    }
    
    /**
     * HttpMessageConverter 우선순위를 조정합니다.
     * 
     * <p>MappingJackson2HttpMessageConverter를 StringHttpMessageConverter보다 
     * 앞에 배치하여 ResponseAdvice가 JSON 직렬화를 처리할 수 있도록 합니다.
     * 
     * @param converters 메시지 컨버터 리스트
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        logger.debug("Configuring HttpMessageConverter order for ResponseAdvice compatibility");
        
        // StringHttpMessageConverter를 찾아서 제거
        HttpMessageConverter<?> stringConverter = null;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                stringConverter = converters.remove(i);
                logger.debug("Removed StringHttpMessageConverter from position {}", i);
                break;
            }
        }
        
        // MappingJackson2HttpMessageConverter 뒤에 StringHttpMessageConverter 추가
        if (stringConverter != null) {
            boolean jacksonFound = false;
            for (int i = 0; i < converters.size(); i++) {
                if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                    converters.add(i + 1, stringConverter);
                    logger.debug("Added StringHttpMessageConverter after MappingJackson2HttpMessageConverter at position {}", i + 1);
                    jacksonFound = true;
                    break;
                }
            }
            
            // Jackson 컨버터가 없으면 마지막에 추가
            if (!jacksonFound) {
                converters.add(stringConverter);
                logger.debug("Added StringHttpMessageConverter at the end (Jackson converter not found)");
            }
        }
        
        logger.debug("Final converter order:");
        for (int i = 0; i < converters.size(); i++) {
            logger.debug("  {}: {}", i, converters.get(i).getClass().getSimpleName());
        }
    }
}