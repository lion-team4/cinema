package com.example.cinema.dto.schedule;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 일일 편성 등록 요청 DTO
 * <p>
 * 용도:
 * - 특정 날짜에 여러 상영 스케줄을 한 번에 등록 (POST /schedules/{date})
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleCreateRequest {

    /**
     * 편성할 콘텐츠 ID
     */
    @NotNull(message = "Content ID is required")
    private Long contentId;

    /**
     * 편성 날짜 (오늘 혹은 미래여야 함)
     */
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Schedule date must be today or in the future")
    private LocalDate scheduleDate;

    /**
     * 상영 시간 슬롯 목록
     */
    @NotNull(message = "Slot list is required")
    private List<SlotInfo> slots;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SlotInfo {
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
    }
}