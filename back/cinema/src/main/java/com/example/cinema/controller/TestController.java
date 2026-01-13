package com.example.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 테스트용 HTML 페이지 제공 컨트롤러
 */
@Controller
public class TestController {

    /**
     * 구독 결제 테스트 페이지
     * GET /subscription-test
     */
    @GetMapping("/subscription-test")
    public String subscriptionTest() {
        return "forward:/subscription-test.html";
    }
}

