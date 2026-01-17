package com.example.cinema.controller.test;

import com.example.cinema.dto.auth.LoginRequest;
import com.example.cinema.dto.auth.TokenResponse;
import com.example.cinema.entity.User;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket 채팅 테스트용 컨트롤러
 * dev, test 프로필에서만 활성화
 * 
 * 사용법:
 * 1. /test/ws/login?email=xxx&password=xxx 로 로그인하여 토큰 발급
 * 2. /test/ws/chat?scheduleId=1 로 채팅 테스트 페이지 접속
 */
@Controller
@RequestMapping("/test/ws")
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class WebSocketTestController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 로그인 페이지
     * GET /test/ws/login
     */
    @GetMapping("/login")
    public String loginPage() {
        return "test/ws-login";
    }

    /**
     * 로그인 처리 후 토큰과 함께 채팅 페이지로 이동
     * POST /test/ws/login
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "1") Long scheduleId,
            Model model) {
        try {
            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(password);
            
            TokenResponse tokenResponse = userService.login(request);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            model.addAttribute("accessToken", tokenResponse.getAccessToken());
            model.addAttribute("nickname", user.getNickname());
            model.addAttribute("scheduleId", scheduleId);
            
            return "test/ws-chat";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "test/ws-login";
        }
    }

    /**
     * 토큰을 직접 입력하여 채팅 테스트
     * GET /test/ws/chat?scheduleId=1&token=xxx
     */
    @GetMapping("/chat")
    public String chatPage(
            @RequestParam(defaultValue = "1") Long scheduleId,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String nickname,
            Model model) {
        
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("accessToken", token != null ? token : "");
        model.addAttribute("nickname", nickname != null ? nickname : "Guest");
        
        return "test/ws-chat";
    }
}
