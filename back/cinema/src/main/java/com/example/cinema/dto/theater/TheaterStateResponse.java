package com.example.cinema.dto.theater;

import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.type.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;

/**
 * 상영관 상태 응답 DTO (상세 정보 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterStateResponse {

    private Long scheduleId;

    private ScheduleStatus status;

    private long serverTimeMs;

    private long startAtMs;

    private long durationMs;

    private long positionMs;

    /** WAITING 상태 시작 시간 (startAt - 10분) */
    private long waitingOpenAtMs;

    /** CLOSED 상태 시작 시간 (endAt + 10분) */
    private long endingCloseAtMs;

    public static TheaterStateResponse from(ScheduleItem item, long serverTimeMs, long durationMs) {
        if (item == null) {
            return notFound(serverTimeMs);
        }

        ScheduleStatus status = item.getStatus();
        long startAtMs = item.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli();
        long endAtMs = item.getEndAt().toInstant(ZoneOffset.UTC).toEpochMilli();

        long positionMs = 0L;
        if (status == ScheduleStatus.PLAYING) {
            positionMs = Math.max(0L, serverTimeMs - startAtMs);
        }

        return TheaterStateResponse.builder()
                .scheduleId(item.getScheduleItemId())
                .status(status)
                .serverTimeMs(serverTimeMs)
                .startAtMs(startAtMs)
                .durationMs(durationMs)
                .positionMs(positionMs)
                .waitingOpenAtMs(startAtMs - (10 * 60 * 1000)) // 10분 전
                .endingCloseAtMs(endAtMs + (10 * 60 * 1000))   // 10분 후
                .build();
    }

    public static TheaterStateResponse notFound(long serverTimeMs) {
        return TheaterStateResponse.builder()
                .scheduleId(null)
                .status(ScheduleStatus.CLOSED)
                .serverTimeMs(serverTimeMs)
                .startAtMs(0L)
                .durationMs(0L)
                .positionMs(0L)
                .waitingOpenAtMs(0L)
                .endingCloseAtMs(0L)
                .build();
    }
}
