package com.example.cinema.dto.theater;

import com.example.cinema.entity.WatchHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상영관 퇴장 결과 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterLeaveResponse {

    private Long watchId;

    private Long scheduleItemId;

    private String contentTitle;

    private LocalDateTime enteredAt;

    private LocalDateTime leftAt;

    public static TheaterLeaveResponse from(WatchHistory history) {
        return TheaterLeaveResponse.builder()
                .watchId(history.getWatchId())
                .scheduleItemId(history.getScheduleItem().getScheduleItemId())
                .contentTitle(history.getScheduleItem().getContent().getTitle())
                .enteredAt(history.getCreatedAt())
                .leftAt(history.getLeftAt())
                .build();
    }

    /**
     * 입장 기록이 없을 때 빈 응답 반환
     */
    public static TheaterLeaveResponse empty() {
        return TheaterLeaveResponse.builder().build();
    }
}
