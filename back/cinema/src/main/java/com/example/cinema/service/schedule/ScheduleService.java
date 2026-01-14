package com.example.cinema.service.schedule;

import com.example.cinema.dto.schedule.*;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.ScheduleDay;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.entity.User;
import com.example.cinema.repository.ContentRepository;
import com.example.cinema.repository.schedule.ScheduleDayRepository;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.ContentStatus;
import com.example.cinema.type.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final UserRepository userRepository;

    /**
     * 특정 날짜의 전체 상영 일정을 조회합니다.
     */
    public ScheduleDayResponse getScheduleDay(Long contentId, LocalDate date) {
        ScheduleDay scheduleDay = scheduleDayRepository.findByContent_ContentIdAndScheduleDate(contentId, date)
                .orElseThrow(() -> new IllegalArgumentException("No schedule found for this date."));

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
    public ScheduleCreateResponse createSchedule(ScheduleCreateRequest request, String email) {
        Content content = getContent(request.getContentId());
        User requester = getUser(email);

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
                .status(ScheduleStatus.COMING_UP)
                .build());

        return new ScheduleCreateResponse(scheduleItem.getScheduleItemId());
    }

    /**
     * 기존 상영 일정의 상영 시간을 수정합니다.
     */
    @Transactional
    public ScheduleItemResponse editSchedule(Long scheduleItemId, ScheduleEditRequest request, String email) {
        ScheduleItem item = validateModifyAccess(scheduleItemId, email);

        validateTimeRange(request.getStartAt(), request.getEndAt());
        validateCreatorOverlap(item.getContent().getOwner().getUserId(), request.getStartAt(), request.getEndAt(), item.getScheduleItemId());

        item.update(request.getStartAt(), request.getEndAt());
        return ScheduleItemResponse.from(item);
    }

    /**
     * 상영 일정을 삭제합니다.
     */
    @Transactional
    public void deleteSchedule(Long scheduleItemId, String email) {
        ScheduleItem item = validateModifyAccess(scheduleItemId, email);
        scheduleItemRepository.delete(item);
    }

    /**
     * 특정 날짜의 모든 편성을 확정(Lock) 처리합니다.
     */
    @Transactional
    public void lockSchedule(Long scheduleDayId, boolean isLock, String email) {
        ScheduleDay scheduleDay = getScheduleDayEntity(scheduleDayId);
        User requester = getUser(email);

        validateOwner(requester.getUserId(), scheduleDay.getContent().getOwner().getUserId());

        if (scheduleDay.getIsLocked()) {
            throw new IllegalStateException("This schedule is already confirmed and cannot be modified.");
        }

        scheduleDay.updateLock(isLock, isLock ? LocalDateTime.now() : null);
    }

    // --- Private Helper Methods ---

    private ScheduleItem validateModifyAccess(Long scheduleItemId, String email) {
        ScheduleItem item = getScheduleItem(scheduleItemId);
        User requester = getUser(email);

        validateOwner(requester.getUserId(), item.getContent().getOwner().getUserId());
        validateScheduleUnlocked(item.getScheduleDay());

        return item;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
    }

    private Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found with id: " + contentId));
    }

    private ScheduleItem getScheduleItem(Long scheduleItemId) {
        return scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule Item not found with id: " + scheduleItemId));
    }

    private ScheduleDay getScheduleDayEntity(Long scheduleDayId) {
        return scheduleDayRepository.findById(scheduleDayId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule Day not found with id: " + scheduleDayId));
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
            throw new AccessDeniedException("Access denied: You are not the owner of this content.");
        }
    }

    private void validateContentStatus(Content content) {
        if (content.getStatus() != ContentStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED content can be scheduled.");
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

    private void validateScheduleUnlocked(ScheduleDay scheduleDay) {
        if (scheduleDay.getIsLocked()) {
            throw new IllegalStateException("Schedule Day is locked. Cannot modify schedule.");
        }
    }

    private void validateCreatorOverlap(Long ownerId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (scheduleItemRepository.existsOverlapByOwner(ownerId, start, end, excludeId)) {
            throw new IllegalStateException("The creator already has a schedule during this time.");
        }
    }
}