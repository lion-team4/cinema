package com.example.cinema.controller.theater;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.theater.PlaybackInfoResponse;
import com.example.cinema.entity.User;
import com.example.cinema.service.theater.TheaterPlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/theaters")
public class TheaterPlaybackController {

    private final TheaterPlaybackService playbackService;

    @GetMapping("/{scheduleId}/playback")
    public ResponseEntity<ApiResponse<PlaybackInfoResponse>> playback(@PathVariable long scheduleId,
                                                                      Principal principal) {

        PlaybackInfoResponse info = playbackService.getPlaybackInfo(scheduleId, principal.getName());
        if (info == null) {
            return ResponseEntity.ok(ApiResponse.error("재생 정보를 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success("재생 정보 조회 성공", info));
    }
}
