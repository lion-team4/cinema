package com.example.cinema.controller.test;


import com.example.cinema.config.TossPaymentConfig;
import com.example.cinema.entity.Payment;
import com.example.cinema.entity.User;
import com.example.cinema.infrastructure.payment.toss.dto.TossBillingResponse;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.test.BillingTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class BillingController {

    private final BillingTestService billingTestService;
    private final TossPaymentConfig tossPaymentConfig;
    private final UserRepository userRepository;

        /**
     * 테스트 유저를 생성하고 결제 등록 페이지로 리다이렉트합니다.
     */
    @GetMapping("/create-user")
    public String createTestUser(@RequestParam String nickname) {
        billingTestService.createTestUser(nickname, null);
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
        model.addAttribute("customerKey", user.getNickname());
        model.addAttribute("customerEmail", user.getEmail());
        model.addAttribute("customerName", user.getNickname());
        
        return "test/index";
    }

        /**
     * 빌링키 발급 성공 시 호출되는 콜백 페이지입니다.
     */
    @GetMapping("/success")
    public String successPage(@RequestParam String authKey, @RequestParam String customerKey, Model model) {
        TossBillingResponse response = billingTestService.registerBillingKey(authKey, customerKey);
        model.addAttribute("billingResponse", response);
        model.addAttribute("nickname", customerKey);
        return "test/success";
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
     * 실제 결제를 처리합니다.
     */
    @PostMapping("/pay")
    public String processPayment(@RequestParam String nickname, @RequestParam Long amount, Model model) {
        try {
            billingTestService.pay(nickname, amount);
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
        List<Payment> history = billingTestService.getHistory(nickname);
        model.addAttribute("history", history);
        model.addAttribute("nickname", nickname);
        return "test/history";
    }

        /**
     * 빌링키를 삭제하고 구독을 취소합니다.
     */
    @PostMapping("/billing/delete")
    public String deleteBillingKey(@RequestParam String nickname) {
        billingTestService.removeBillingKey(nickname);
        return "redirect:/test/billing?nickname=" + nickname;
    }
}
