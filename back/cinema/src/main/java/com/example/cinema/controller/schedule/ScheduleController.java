package com.example.cinema.controller.schedule;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.schedule.*;
import com.example.cinema.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;


    /**
     * 새로운 상영 일정을 생성합니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleCreateResponse>> createSchedule(
            @RequestBody ScheduleCreateRequest request,
            Principal principal) {
        ScheduleCreateResponse response = scheduleService.createSchedule(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("상영 일정이 성공적으로 생성되었습니다.", response));
    }

    /**
     * 기존 상영 일정의 시간을 수정합니다.
     */
    @PutMapping("/{scheduleItemId}")
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> editSchedule(
            @PathVariable Long scheduleItemId,
            @RequestBody ScheduleEditRequest request,
            Principal principal) {
        ScheduleItemResponse response = scheduleService.editSchedule(scheduleItemId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("상영 일정이 성공적으로 수정되었습니다.", response));
    }

    /**
     * 특정 날짜의 편성을 확정(Lock) 처리합니다.
     */
    @PutMapping("/{scheduleDayId}/confirm")
    public ResponseEntity<ApiResponse<ScheduleLockResponse>> lockSchedule(
            @PathVariable Long scheduleDayId,
            @RequestBody ScheduleLockRequest request,
            Principal principal) {
        ScheduleLockResponse response = scheduleService.lockSchedule(scheduleDayId, request.getIsLock(), principal.getName());
        String message = response.getIsLocked() ? "편성이 확정되었습니다." : "편성 확정이 취소되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 상영 일정을 삭제합니다.
     */
    @DeleteMapping("/{scheduleItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable Long scheduleItemId,
            Principal principal) {
        scheduleService.deleteSchedule(scheduleItemId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("상영 일정이 성공적으로 삭제되었습니다."));
    }
}