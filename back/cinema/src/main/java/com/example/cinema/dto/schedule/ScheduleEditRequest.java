package com.example.cinema.dto.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleEditRequest {
    @NotNull
    private LocalDateTime startAt; //시작 시간

    @NotNull
    private LocalDateTime endAt; //종료시간
}
