package com.example.cinema.type;

public enum ScheduleStatus {
    WAITING,   // 시작 10분 전 ~ 시작 시각
    PLAYING,   // 시작 ~ 시작+duration
    ENDING,    // 끝 ~ 끝+10분
    CLOSED     // 그 외(입장 불가 / 킥)
}
