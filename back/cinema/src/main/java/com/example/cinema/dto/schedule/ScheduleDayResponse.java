package com.example.cinema.dto.schedule;

import com.example.cinema.entity.ScheduleDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDayResponse {

    private Long scheduleDayId;
    private Long contentId;
    private String contentTitle;
    private LocalDate scheduleDate;
    private Boolean isLocked;
    private LocalDateTime lockedAt;
    private List<ScheduleItemResponse> items;

    public static ScheduleDayResponse from(ScheduleDay scheduleDay, List<ScheduleItemResponse> items) {
        return ScheduleDayResponse.builder()
                .scheduleDayId(scheduleDay.getScheduleDayId())
                .contentId(scheduleDay.getContent().getContentId())
                .contentTitle(scheduleDay.getContent().getTitle())
                .scheduleDate(scheduleDay.getScheduleDate())
                .isLocked(scheduleDay.getIsLocked())
                .lockedAt(scheduleDay.getLockedAt())
                .items(items)
                .build();
    }
}
