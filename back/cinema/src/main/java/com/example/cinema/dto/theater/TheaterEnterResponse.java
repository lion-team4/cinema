package com.example.cinema.dto.theater;

import com.example.cinema.entity.WatchHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상영관 입장 결과 응답 DTO
 * <p>
 * 용도:
 * - 상영관 입장 시도 결과 반환 (POST /theaters/{id}/enter)
 * - 클라이언트 재생 동기화 시점 제공
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterEnterResponse {

    /** 생성된 시청 이력 ID (추후 리뷰 작성 자격 증명 등에 사용) */
    private Long watchId;

    private Long scheduleItemId;

    private String contentTitle;

    /** 입장 시각 (서버 시간 기준, 재생 위치 동기화에 중요) */
    private LocalDateTime enteredAt;

    public static TheaterEnterResponse from(WatchHistory history) {
        return TheaterEnterResponse.builder()
                .watchId(history.getWatchId())
                .scheduleItemId(history.getScheduleItem().getScheduleItemId())
                .contentTitle(history.getScheduleItem().getContent().getTitle())
                .enteredAt(history.getCreatedAt())
                .build();
    }
}
