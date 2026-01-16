package com.example.cinema.controller.theater;

import com.example.cinema.config.common.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.theater.TheaterEnterResponse;
import com.example.cinema.dto.theater.TheaterLeaveResponse;
import com.example.cinema.service.theater.TheaterEnterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/theaters")
public class TheaterEnterController {

    private final TheaterEnterService enterService;

    /**
     * 상영관 입장
     * - 구독 상태 검증
     * - WAITING 또는 PLAYING 상태에서만 입장 가능
     */
    @PostMapping("/{scheduleId}/enter")
    public ResponseEntity<ApiResponse<TheaterEnterResponse>> enter(
            @PathVariable long scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TheaterEnterResponse response = enterService.enter(scheduleId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success("상영관 입장 성공", response));
    }

    /**
     * 상영관 퇴장
     */
    @PostMapping("/{scheduleId}/leave")
    public ResponseEntity<ApiResponse<TheaterLeaveResponse>> leave(
            @PathVariable long scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TheaterLeaveResponse response = enterService.leave(scheduleId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success("상영관 퇴장 성공", response));
    }

    /**
     * 현재 시청자 수 조회
     */
    @GetMapping("/{scheduleId}/viewers")
    public ResponseEntity<ApiResponse<Long>> getViewerCount(@PathVariable long scheduleId) {
        long count = enterService.getViewerCount(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("시청자 수 조회 성공", count));
    }
}
