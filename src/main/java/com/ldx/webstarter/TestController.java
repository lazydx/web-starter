package com.ldx.webstarter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개선사항 테스트용 컨트롤러.
 */
@RestController
public class TestController {
    
    @GetMapping("/test/string")
    public String getString() {
        return "Hello, Web-Starter!";
    }
    
    @GetMapping("/test/number")
    public Integer getNumber() {
        return 42;
    }
    
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