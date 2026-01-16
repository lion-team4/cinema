package com.example.cinema.dto.theater;

import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.type.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * 재생 상태 응답 DTO
 * - 스케줄 시작 시간 기준 자동 계산
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackStateResponse {

    /** 스케줄 상태 (WAITING, PLAYING, ENDING, CLOSED) */
    private ScheduleStatus status;

    /** 재생 중 여부 (PLAYING 상태일 때 true) */
    private boolean playing;

    /** 현재 재생 위치 (스케줄 시작 시간부터 경과된 밀리초) */
    private long positionMs;

    /** 재생 속도 (항상 1.0) */
    private double playbackRate;

    /** 서버 현재 시간 (밀리초) */
    private long serverTimeMs;

    public static PlaybackStateResponse from(ScheduleItem item, long serverTimeMs) {
        if (item == null) {
            return notPlaying(serverTimeMs);
        }

        ScheduleStatus status = item.getStatus();
        boolean isPlaying = status == ScheduleStatus.PLAYING;

        long positionMs = 0L;
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        if (isPlaying) {
            long startAtMs = item.getStartAt().atZone(zoneId).toInstant().toEpochMilli();
            positionMs = Math.max(0L, serverTimeMs - startAtMs);
        }

        return PlaybackStateResponse.builder()
                .status(status)
                .playing(isPlaying)
                .positionMs(positionMs)
                .playbackRate(1.0)
                .serverTimeMs(serverTimeMs)
                .build();
    }

    public static PlaybackStateResponse notPlaying(long serverTimeMs) {
        return PlaybackStateResponse.builder()
                .status(ScheduleStatus.CLOSED)
                .playing(false)
                .positionMs(0L)
                .playbackRate(1.0)
                .serverTimeMs(serverTimeMs)
                .build();
    }
}
