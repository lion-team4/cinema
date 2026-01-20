package com.example.cinema.dto.schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


// 유저의 전체 상영일정을 조회하는 요청
@Setter
@Getter
public class ScheduleSearchRequest {
    private Integer page = 0;
    private Integer size = 10;
    private boolean dateFilter = false;
    private LocalDate startDate;
    private LocalDate endDate;
    private String nickname;
}
