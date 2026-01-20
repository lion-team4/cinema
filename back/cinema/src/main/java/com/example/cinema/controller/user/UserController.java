package com.example.cinema.controller.user;

import com.example.cinema.config.common.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.theater.TheaterLogResponse;
import com.example.cinema.dto.user.UserDeleteRequest;
import com.example.cinema.dto.user.UserGetResponse;
import com.example.cinema.dto.user.UserUpdateResponse;
import com.example.cinema.dto.user.UserUpdateRequest;
import com.example.cinema.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserGetResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userService.getProfile(userDetails.getUser().getUserId())));
    }

    @Operation(summary = "내 정보 수정", description = "로그인된 사용자의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 수정 성공", userService.updateProfile(userDetails.getUser().getUserId(), request)));
    }

    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 회원을 탈퇴 처리(Soft Delete)합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserDeleteRequest request) {
        
        userService.deleteUser(userDetails.getUser().getUserId(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }

    @Operation(summary = "시청 기록 조회", description = "로그인한 사용자의 시청 기록을 조회합니다.")
    @GetMapping("/me/watch-history")
    public ResponseEntity<ApiResponse<List<TheaterLogResponse>>> getWatchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TheaterLogResponse> response = userService.getWatchHistory(userDetails.getUser().getUserId());
        return ResponseEntity.ok(ApiResponse.success("시청 기록 조회 성공", response));
    }
}
