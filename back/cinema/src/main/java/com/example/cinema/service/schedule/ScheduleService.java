package com.example.cinema.service.schedule;

import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.schedule.*;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.ScheduleDay;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.entity.User;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.schedule.ScheduleDayRepository;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.type.ContentStatus;
import com.example.cinema.type.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 영화 상영 일정(스케줄) 관리를 담당하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ContentRepository contentRepository;

    /**
     * 상영 일정을 검색합니다.
     */
    public PageResponse<ScheduleSearchResponse> searchSchedules(ScheduleSearchRequest request) {
        Page<ScheduleItem> page = scheduleItemRepository.search(request);
        return PageResponse.from(page.map(ScheduleSearchResponse::from));
    }

    /**
     * 특정 날짜의 전체 상영 일정을 조회합니다.
     */
    public ScheduleDayResponse getScheduleDay(Long contentId, LocalDate date) {
        ScheduleDay scheduleDay = scheduleDayRepository.findByContent_ContentIdAndScheduleDate(contentId, date)
                .orElseThrow(() -> new BusinessException("해당 날짜의 상영 일정을 찾을 수 없습니다.", ErrorCode.SCHEDULE_NOT_FOUND));

        List<ScheduleItemResponse> items = scheduleItemRepository.findAllByScheduleDay_ScheduleDayId(scheduleDay.getScheduleDayId())
                .stream()
                .map(ScheduleItemResponse::from)
                .collect(Collectors.toList());

        return ScheduleDayResponse.from(scheduleDay, items);
    }

    /**
     * 새로운 상영 일정(슬롯)을 생성합니다.
     */
    @Transactional
    public ScheduleCreateResponse createSchedule(ScheduleCreateRequest request, User requester) {
        Content content = getContent(request.getContentId());

        validateOwner(requester.getUserId(), content.getOwner().getUserId());
        validateContentStatus(content);
        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateCreatorOverlap(content.getOwner().getUserId(), request.getStartAt(), request.getEndAt(), null);

        ScheduleDay scheduleDay = getOrCreateScheduleDay(content, request.getScheduleDate());
        validateScheduleUnlocked(scheduleDay);

        ScheduleItem scheduleItem = scheduleItemRepository.save(ScheduleItem.builder()
                .scheduleDay(scheduleDay)
                .content(content)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(ScheduleStatus.CLOSED)
                .build());

        return new ScheduleCreateResponse(scheduleItem.getScheduleItemId(), scheduleDay.getScheduleDayId());
    }

    /**
     * 기존 상영 일정의 상영 시간을 수정합니다.
     */
    @Transactional
    public ScheduleItemResponse editSchedule(Long scheduleItemId, ScheduleEditRequest request, User requester) {
        ScheduleItem item = validateModifyAccess(scheduleItemId, requester);

        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateCreatorOverlap(item.getContent().getOwner().getUserId(), request.getStartAt(), request.getEndAt(), item.getScheduleItemId());

        item.update(request.getStartAt(), request.getEndAt());
        return ScheduleItemResponse.from(item);
    }

    /**
     * 특정 상영 일정 상세 조회
     */
    public ScheduleItemResponse getScheduleItemInfo(Long scheduleItemId) {
        ScheduleItem item = getScheduleItem(scheduleItemId);
        return ScheduleItemResponse.from(item);
    }

    /**
     * 상영 일정을 삭제합니다.
     */
    @Transactional
    public void deleteSchedule(Long scheduleItemId, User requester) {
        ScheduleItem item = validateModifyAccess(scheduleItemId, requester);
        scheduleItemRepository.delete(item);
    }

    /**
     * 특정 날짜의 모든 편성을 확정(Lock) 처리합니다.
     */
    @Transactional
    public ScheduleLockResponse lockSchedule(Long scheduleDayId, boolean isLock, User requester) {
        ScheduleDay scheduleDay = getScheduleDayEntity(scheduleDayId);

        validateOwner(requester.getUserId(), scheduleDay.getContent().getOwner().getUserId());

        if (scheduleDay.getIsLocked()) {
            throw new BusinessException("이미 확정된 스케줄이므로 수정할 수 없습니다.", ErrorCode.INVALID_INPUT_VALUE);
        }

        scheduleDay.updateLock(isLock, isLock ? LocalDateTime.now() : null);
        return ScheduleLockResponse.from(scheduleDay);
    }

    // --- Private Helper Methods ---

    private ScheduleItem validateModifyAccess(Long scheduleItemId, User requester) {
        ScheduleItem item = getScheduleItem(scheduleItemId);

        validateOwner(requester.getUserId(), item.getContent().getOwner().getUserId());
        validateScheduleUnlocked(item.getScheduleDay());

        return item;
    }

    private Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException("콘텐츠를 찾을 수 없습니다. ID: " + contentId, ErrorCode.CONTENT_NOT_FOUND));
    }

    private ScheduleItem getScheduleItem(Long scheduleItemId) {
        return scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new BusinessException("상영 일정을 찾을 수 없습니다. ID: " + scheduleItemId, ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private ScheduleDay getScheduleDayEntity(Long scheduleDayId) {
        return scheduleDayRepository.findById(scheduleDayId)
                .orElseThrow(() -> new BusinessException("스케줄 일자를 찾을 수 없습니다. ID: " + scheduleDayId, ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private ScheduleDay getOrCreateScheduleDay(Content content, LocalDate date) {
        return scheduleDayRepository.findByContent_ContentIdAndScheduleDate(content.getContentId(), date)
                .orElseGet(() -> scheduleDayRepository.save(ScheduleDay.builder()
                        .content(content)
                        .scheduleDate(date)
                        .isLocked(false)
                        .build()));
    }

    private void validateOwner(Long requesterId, Long ownerId) {
        if (!requesterId.equals(ownerId)) {
            throw new BusinessException("접근 권한이 없습니다. 본인의 콘텐츠만 관리할 수 있습니다.", ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateContentStatus(Content content) {
        if (content.getStatus() != ContentStatus.PUBLISHED) {
            throw new BusinessException("상영 승인(PUBLISHED)된 콘텐츠만 스케줄을 등록할 수 있습니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BusinessException("시작 시간은 종료 시간보다 앞서야 합니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateScheduleUnlocked(ScheduleDay scheduleDay) {
        if (scheduleDay.getIsLocked()) {
            throw new BusinessException("스케줄이 확정(Lock)되어 수정할 수 없습니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateCreatorOverlap(Long ownerId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (scheduleItemRepository.existsOverlapByOwner(ownerId, start, end, excludeId)) {
            throw new BusinessException("해당 시간에 이미 등록된 스케줄이 존재합니다.", ErrorCode.SCHEDULE_CONFLICT);
        }
    }
}
