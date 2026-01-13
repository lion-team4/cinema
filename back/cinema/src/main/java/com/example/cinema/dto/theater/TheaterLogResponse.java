package com.example.cinema.dto.theater;

import com.example.cinema.entity.WatchHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 시청 기록 로그 응답 DTO
 * <p>
 * 용도:
 * - 사용자의 시청 내역 조회 (GET /theaters/logs)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TheaterLogResponse {
    private final Long watchId;
    private final String contentTitle;
    private final LocalDateTime viewedAt;

    public static TheaterLogResponse from(WatchHistory history) {
        return new TheaterLogResponse(
                history.getWatchId(),
                history.getScheduleItem().getContent().getTitle(),
                history.getCreatedAt()
        );
    }
}