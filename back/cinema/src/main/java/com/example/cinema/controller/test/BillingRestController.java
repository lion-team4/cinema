package com.example.cinema.controller.test;


import com.example.cinema.config.TossPaymentConfig;
import com.example.cinema.infrastructure.payment.toss.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import com.example.cinema.infrastructure.payment.toss.TossPaymentClient;
import com.example.cinema.infrastructure.payment.toss.dto.TossPaymentResponse;
import com.example.cinema.service.test.BillingTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingRestController {
    
    private final BillingTestService billingTestService;

        /**
     * (API) 등록된 빌링키로 결제를 요청합니다.
     */
    @PostMapping("/pay")
    public TossPaymentResponse pay(@RequestParam String nickname, @RequestParam Long amount) {
        return billingTestService.pay(nickname, amount);
    }
}
