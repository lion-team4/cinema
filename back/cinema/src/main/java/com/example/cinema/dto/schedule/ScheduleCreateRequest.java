package com.example.cinema.dto.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequest {
    @NotNull
    private Long contentId; //영화

    @NotNull
    private LocalDate scheduleDate; //편성날짜

    @NotNull
    private LocalDateTime startAt; //시작 시간

    @NotNull
    private LocalDateTime endAt; //종료시간
}
