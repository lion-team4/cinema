package com.example.cinema.controller.theater;

import com.example.cinema.dto.theater.ChatRequest;
import com.example.cinema.dto.theater.ChatResponse;
import com.example.cinema.dto.theater.PlaybackStateResponse;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.theater.TheaterSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
/**
 * 영상 동시 송출 WebSocket 컨트롤러
 * - 재생 제어 없음 (스케줄 시작 시간 기준 자동 재생)
 * - 클라이언트가 구독 시 현재 재생 상태 반환
 * - 실시간 채팅 기능
 */
@Controller
@RequiredArgsConstructor
public class TheaterWsController {

    private final TheaterSyncService syncService;
    private final UserRepository userRepository;

    /**
     * 재생 상태 구독
     * 클라이언트가 /app/theaters/{scheduleId}/state 구독 시 현재 상태 반환
     */
    @SubscribeMapping("/theaters/{scheduleId}/state")
    public PlaybackStateResponse subscribeState(@DestinationVariable long scheduleId) {
        return syncService.getState(scheduleId);
    }

    /**
     * 채팅 메시지 전송
     * - 클라이언트: /app/chat/{scheduleId}로 메시지 전송
     * - 서버: /topic/theaters/{scheduleId}/chat으로 브로드캐스트
     * - CustomUserDetails로 인증된 사용자 정보 사용 (위조 방지)
     */
    @MessageMapping("/chat/{scheduleId}")
    @SendTo("/topic/theaters/{scheduleId}/chat")
    public ChatResponse sendChat(
            @DestinationVariable long scheduleId,
            ChatRequest request,
            Principal principal) {

        // 인증된 사용자의 닉네임 사용 (이메일 노출 방지)
        String nickname = userRepository.findByEmail(principal.getName())
                .map(user -> user.getNickname())
                .orElse("익명");
        return ChatResponse.from(scheduleId, request.getMessage(), nickname);
    }
}
