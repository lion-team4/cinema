package com.example.cinema.controller.auth;

import com.example.cinema.dto.auth.LoginRequest;
import com.example.cinema.dto.auth.SignupRequest;
import com.example.cinema.dto.auth.TokenResponse;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.user.UserGetResponse;
import com.example.cinema.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserGetResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserGetResponse response = userService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공하였습니다.", response));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 Access Token과 Refresh Token을 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@RequestBody com.example.cinema.dto.auth.TokenRefreshRequest request) {
        TokenResponse response = userService.reissue(request);
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리합니다. 서버의 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // DB에서 Refresh Token 삭제
            userService.logout(auth.getName());

            // Security Context 초기화
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }
}