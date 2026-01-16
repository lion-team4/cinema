package com.example.cinema.controller.theater;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.theater.PlaybackStateResponse;
import com.example.cinema.service.theater.TheaterSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/theaters")
public class TheaterStateController {

    private final TheaterSyncService syncService;

    @GetMapping("/{scheduleId}/state")
    public ResponseEntity<ApiResponse<PlaybackStateResponse>> state(@PathVariable long scheduleId) {
        return ResponseEntity.ok(ApiResponse.success("상영 상태 조회 성공", syncService.getState(scheduleId)));
    }
}
