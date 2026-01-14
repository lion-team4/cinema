package com.example.cinema.controller.test;

import com.example.cinema.entity.Subscription;
import com.example.cinema.entity.User;
import com.example.cinema.repository.subscription.SubscriptionRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/billing")
@RequiredArgsConstructor
@Profile("dev")
@Slf4j
@Profile("dev")
public class BillingRestController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * (API) 등록된 빌링키로 결제를 요청합니다.
     * 테스트용: 사용자의 구독 정보를 찾아 정기 결제를 수동 트리거함.
     * 금액은 Subscription 엔티티의 가격을 따르므로 amount 파라미터는 무시됨.
     */
    @PostMapping("/pay")
    public String pay(@RequestParam String nickname, @RequestParam(required = false) Long amount) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscriptionService.processRecurringPayment(subscription.getSubscriptionId());

        return "Payment processed successfully (Amount determined by subscription plan)";
    }
}
