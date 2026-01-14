package com.example.cinema.dto.schedule;

import com.example.cinema.entity.ScheduleDay;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleLockResponse {
    private Long scheduleDayId;
    private Boolean isLocked;
    private LocalDateTime lockedAt;

    public static ScheduleLockResponse from(ScheduleDay scheduleDay) {
        return ScheduleLockResponse.builder()
                .scheduleDayId(scheduleDay.getScheduleDayId())
                .isLocked(scheduleDay.getIsLocked())
                .lockedAt(scheduleDay.getLockedAt())
                .build();
    }
}