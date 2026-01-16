package com.example.cinema.exception;

/**
 * 상영 일정 등록/수정 시, 해당 상영관에 이미 다른 일정이 겹쳐있을 때 발생하는 예외입니다.
 */
public class ScheduleConflictException extends BusinessException {

    public ScheduleConflictException(String message) {
        super(message, ErrorCode.SCHEDULE_CONFLICT);
    }
}
