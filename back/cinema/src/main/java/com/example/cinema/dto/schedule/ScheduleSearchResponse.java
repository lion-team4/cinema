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
public class ScheduleSearchResponse {

    private Long scheduleItemId;
    private Long contentId;
    private String contentTitle;
    private String creatorNickname;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ScheduleStatus status;
    private Boolean isLocked;
    private Long scheduleDayId;

    public static ScheduleSearchResponse from(ScheduleItem item) {
        return ScheduleSearchResponse.builder()
                .scheduleItemId(item.getScheduleItemId())
                .contentId(item.getContent().getContentId())
                .contentTitle(item.getContent().getTitle())
                .creatorNickname(item.getContent().getOwner().getNickname())
                .startAt(item.getStartAt())
                .endAt(item.getEndAt())
                .status(item.getStatus())
                .isLocked(item.getScheduleDay().getIsLocked())
                .scheduleDayId(item.getScheduleDay().getScheduleDayId())
                .build();
    }
}
