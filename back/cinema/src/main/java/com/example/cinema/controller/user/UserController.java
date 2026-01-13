package com.example.cinema.controller.user;

import com.example.cinema.config.auth.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.user.UserDeleteRequest;
import com.example.cinema.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 회원을 탈퇴 처리(Soft Delete)합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserDeleteRequest request) {
        
        userService.deleteUser(userDetails.getUser().getUserId(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
