package com.example.cinema.controller.test;

import com.example.cinema.config.TossPaymentConfig;
import com.example.cinema.dto.subscription.PaymentHistoryResponse;
import com.example.cinema.dto.subscription.SubscriptionCreateRequest;
import com.example.cinema.entity.Payment;
import com.example.cinema.entity.Subscription;
import com.example.cinema.entity.User;
import com.example.cinema.infrastructure.payment.toss.dto.TossBillingResponse;
import com.example.cinema.repository.SubscriptionRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.subscription.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class BillingController {

    private final SubscriptionService subscriptionService;
    private final TossPaymentConfig tossPaymentConfig;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    /**
     * 테스트 유저를 생성하고 결제 등록 페이지로 리다이렉트합니다.
     */
    @GetMapping("/create-user")
    public String createTestUser(@RequestParam String nickname) {
        if (userRepository.findByNickname(nickname).isEmpty()) {
            userRepository.save(User.builder()
                    .email(nickname + "@test.com")
                    .nickname(nickname)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .build());
        }
        return "redirect:/test/billing?nickname=" + nickname;
    }

    /**
     * 카드(빌링키) 등록 페이지를 렌더링합니다.
     */
    @GetMapping("/billing")
    public String billingPage(@RequestParam String nickname, Model model) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        model.addAttribute("clientKey", tossPaymentConfig.getClientKey());
        model.addAttribute("customerKey", user.getNickname()); // 테스트 편의상 닉네임을 키로 사용
        model.addAttribute("customerEmail", user.getEmail());
        model.addAttribute("customerName", user.getNickname());

        return "test/index";
    }

    /**
     * 빌링키 발급 성공 시 호출되는 콜백 페이지입니다.
     * 실제 로직: 빌링키 발급 + 구독 생성 + 초기 결제
     */
    @GetMapping("/success")
    public String successPage(@RequestParam String authKey, @RequestParam String customerKey, Model model) {
        // customerKey = nickname
        User user = userRepository.findByNickname(customerKey)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // DTO 생성 (Reflection/ObjectMapper)
            SubscriptionCreateRequest request = objectMapper.convertValue(
                    Map.of("authKey", authKey),
                    SubscriptionCreateRequest.class
            );

            subscriptionService.createSubscription(user.getUserId(), request);
            
            // 결과 표시용 가짜 응답 객체 (View 호환성)
            TossBillingResponse mockResponse = new TossBillingResponse();
            // 필요한 필드만 대충 채워넣거나 View를 수정해야 함. 
            // 여기서는 View가 billingResponse.billingKey 등을 쓸 수 있으므로 주의.
            // 하지만 SubscriptionService는 SubscriptionResponse를 반환함.
            // 기존 View(test/success.html)가 TossBillingResponse를 기대한다면 맞춰줘야 함.
            // 일단은 성공 메시지로 대체하거나, View에 전달하는 객체를 조정.
            
            model.addAttribute("billingResponse", mockResponse); 
            model.addAttribute("nickname", customerKey);
            model.addAttribute("message", "구독 생성 및 초기 결제 성공!");
            
            return "test/success";
        } catch (Exception e) {
            model.addAttribute("code", "SUBSCRIPTION_FAIL");
            model.addAttribute("message", e.getMessage());
            return "test/fail";
        }
    }

    /**
     * 빌링키 발급 실패 시 호출되는 콜백 페이지입니다.
     */
    @GetMapping("/fail")
    public String failPage(@RequestParam String code, @RequestParam String message, Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "test/fail";
    }

    /**
     * 결제 금액을 입력하는 페이지를 렌더링합니다.
     */
    @GetMapping("/pay")
    public String paymentPage(@RequestParam String nickname, Model model) {
        model.addAttribute("nickname", nickname);
        return "test/payment";
    }

    /**
     * 실제 결제를 처리합니다. (여기서는 정기 결제 로직을 수동 트리거)
     */
    @PostMapping("/pay")
    public String processPayment(@RequestParam String nickname, @RequestParam(required = false) Long amount, Model model) {
        try {
            User user = userRepository.findByNickname(nickname)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Subscription subscription = subscriptionRepository.findBySubscriber(user)
                    .orElseThrow(() -> new RuntimeException("구독 정보 없음"));

            subscriptionService.processRecurringPayment(subscription);
            
            return "redirect:/test/history?nickname=" + nickname;
        } catch (Exception e) {
            model.addAttribute("code", "PAYMENT_ERROR");
            model.addAttribute("message", e.getMessage());
            return "test/fail";
        }
    }

    /**
     * 결제 내역 페이지를 렌더링합니다.
     */
    @GetMapping("/history")
    public String historyPage(@RequestParam String nickname, Model model) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pageable 생략 (전체 조회 불가능하므로 첫 페이지만)
        List<PaymentHistoryResponse> history = subscriptionService.getPaymentHistory(user.getUserId(), null, null, Pageable.unpaged())
                .getContent();

        model.addAttribute("history", history);
        model.addAttribute("nickname", nickname);
        return "test/history";
    }

    /**
     * 빌링키를 삭제하고 구독을 취소합니다.
     */
    @PostMapping("/billing/delete")
    public String deleteBillingKey(@RequestParam String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        subscriptionService.cancelSubscription(user.getUserId());
        
        return "redirect:/test/billing?nickname=" + nickname;
    }
}
