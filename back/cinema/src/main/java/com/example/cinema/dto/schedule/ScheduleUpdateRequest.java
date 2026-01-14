package com.example.cinema.dto.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 편성 슬롯 개별 수정 요청 DTO
 * <p>
 * 용도:
 * - 특정 상영 스케줄의 시간 변경 (PUT /schedules/{id})
 * - 잠금(Locked) 상태가 아닐 때만 가능
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleUpdateRequest {

    @NotNull
    private Long contentId;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;
}