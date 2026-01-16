package com.example.cinema.dto.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequest {
    @NotNull(message = "콘텐츠를 선택해주세요.")
    private Long contentId; //영화

    @NotNull(message = "편성 날짜를 선택해주세요.")
    private LocalDate scheduleDate; //편성날짜

    @NotNull(message = "시작 시간을 입력해주세요.")
    private LocalDateTime startAt; //시작 시간

    @NotNull(message = "종료 시간을 입력해주세요.")
    private LocalDateTime endAt; //종료시간
}
