package com.example.cinema.dto.schedule;

import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.type.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItemResponse {

    private Long scheduleItemId;
    private Long contentId;
    private String contentTitle;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ScheduleStatus status;

    public static ScheduleItemResponse from(ScheduleItem item) {
        return ScheduleItemResponse.builder()
                .scheduleItemId(item.getScheduleItemId())
                .contentId(item.getContent().getContentId())
                .contentTitle(item.getContent().getTitle())
                .startAt(item.getStartAt())
                .endAt(item.getEndAt())
                .status(item.getStatus())
                .build();
    }
}
