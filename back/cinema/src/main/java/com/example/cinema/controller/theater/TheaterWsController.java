package com.example.cinema.controller.theater;

import com.example.cinema.dto.theater.PlaybackStateResponse;
import com.example.cinema.service.theater.TheaterSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * 영상 동시 송출 WebSocket 컨트롤러
 * - 재생 제어 없음 (스케줄 시작 시간 기준 자동 재생)
 * - 클라이언트가 구독 시 현재 재생 상태 반환
 */
@Controller
@RequiredArgsConstructor
public class TheaterWsController {

    private final TheaterSyncService syncService;

    @SubscribeMapping("/theaters/{scheduleId}/state")
    public PlaybackStateResponse subscribeState(@DestinationVariable long scheduleId) {
        return syncService.getState(scheduleId);
    }
}
