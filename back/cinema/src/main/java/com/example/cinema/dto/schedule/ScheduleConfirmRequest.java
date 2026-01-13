package com.example.cinema.dto.schedule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 편성 확정 요청 DTO
 * <p>
 * 용도:
 * - 해당 날짜의 편성을 확정하여 수정 불가능 상태로 변경 (PUT /schedules/{id}/confirm)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleConfirmRequest {
    /**
     * true일 경우 편성 잠금 (확정)
     */
    private Boolean locked = true;
}