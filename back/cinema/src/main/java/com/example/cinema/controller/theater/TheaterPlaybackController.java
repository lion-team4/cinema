package com.example.cinema.controller.theater;

import com.example.cinema.config.common.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.theater.PlaybackInfoResponse;
import com.example.cinema.service.theater.TheaterPlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/theaters")
public class TheaterPlaybackController {

    private final TheaterPlaybackService playbackService;

    @GetMapping("/{scheduleId}/playback")
    public ResponseEntity<ApiResponse<PlaybackInfoResponse>> playback(
            @PathVariable long scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PlaybackInfoResponse info = playbackService.getPlaybackInfo(scheduleId, userDetails.getUser());
        if (info == null) {
            return ResponseEntity.ok(ApiResponse.error("재생 정보를 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success("재생 정보 조회 성공", info));
    }
}
