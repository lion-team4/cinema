package com.example.cinema.service.theater;

import com.example.cinema.dto.theater.TheaterEnterResponse;
import com.example.cinema.dto.theater.TheaterLeaveResponse;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.entity.User;
import com.example.cinema.entity.WatchHistory;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.repository.watchHistory.WatchHistoryRepository;
import com.example.cinema.type.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TheaterEnterService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final ScheduleItemRepository scheduleItemRepository;

    /**
     * 상영관 입장
     * - 구독 상태 검증
     * - 스케줄 상태 검증 (WAITING 또는 PLAYING)
     * - 중복 입장 방지
     */
    @Transactional
    public TheaterEnterResponse enter(long scheduleId, User user) {
        ScheduleItem scheduleItem = getScheduleItem(scheduleId);

        // 구독 상태 검증
        validateSubscription(user);

        // 스케줄 상태 검증 (WAITING 또는 PLAYING만 입장 가능)
        validateScheduleStatus(scheduleItem);

        // 이미 입장한 경우 (퇴장하지 않은 기록이 있으면)
        if (watchHistoryRepository.existsByUserAndScheduleItemAndLeftAtIsNull(user, scheduleItem)) {

            var history= watchHistoryRepository.findByUserAndScheduleItem(user,scheduleItem)
                    .orElseThrow(()-> new IllegalArgumentException("해당하는 유저가 없습니다."));

            history.reEnter();
            history.enter();

            return TheaterEnterResponse.from(history);
        }
        // 시청 기록 생성
        WatchHistory history = watchHistoryRepository.save(WatchHistory.create(user, scheduleItem));

        return TheaterEnterResponse.from(history);
    }

    /**
     * 상영관 퇴장
     */
    @Transactional
    public TheaterLeaveResponse leave(long scheduleId, User user) {
        ScheduleItem scheduleItem = getScheduleItem(scheduleId);

        // 입장 기록 조회 (퇴장하지 않은 기록)
        WatchHistory history = watchHistoryRepository
                .findByUserAndScheduleItemAndLeftAtIsNull(user, scheduleItem)
                .orElseThrow(() -> new IllegalStateException("입장 기록을 찾을 수 없습니다."));

        // 퇴장 처리
        history.leave();
        if (!history.getViewCounted() && history.isDiffMoreThanTenMinutes()){
            scheduleItem.getContent().incrementView();
            history.setViewCounted(true);
        }

        return TheaterLeaveResponse.from(history);
    }

    /**
     * 현재 시청 중인 사용자 수 조회
     */
    public long getViewerCount(long scheduleId) {
        ScheduleItem scheduleItem = getScheduleItem(scheduleId);
        return watchHistoryRepository.countByScheduleItemAndLeftAtIsNull(scheduleItem);
    }

    private ScheduleItem getScheduleItem(long scheduleId) {
        return scheduleItemRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));
    }

    private void validateSubscription(User user) {
        if (user.getSubscription() == null || !user.getSubscription().getIsActive()) {
            throw new AccessDeniedException("구독이 필요한 서비스입니다.");
        }
    }

    private void validateScheduleStatus(ScheduleItem scheduleItem) {
        ScheduleStatus status = scheduleItem.getStatus();
        if (status != ScheduleStatus.WAITING && status != ScheduleStatus.PLAYING) {
            throw new IllegalStateException("현재 입장할 수 없는 상영관입니다. 상태: " + status);
        }
    }
}
