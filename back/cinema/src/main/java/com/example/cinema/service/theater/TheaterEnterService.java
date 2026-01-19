package com.example.cinema.service.theater;

import com.example.cinema.dto.theater.TheaterEnterResponse;
import com.example.cinema.dto.theater.TheaterLeaveResponse;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.entity.User;
import com.example.cinema.entity.WatchHistory;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.repository.watchHistory.WatchHistoryRepository;
import com.example.cinema.type.ScheduleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TheaterEnterService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final UserRepository userRepository;

    /**
     * 상영관 입장
     * - 구독 상태 검증
     * - 스케줄 상태 검증 (WAITING 또는 PLAYING)
     * - 중복 입장 방지
     */
    @Transactional
    public TheaterEnterResponse enter(long scheduleId, User detachedUser) {
        // 영속성 컨텍스트 내에서 User 다시 조회 + 잠금 (중복 입장 방지)
        User user = userRepository.findByIdForUpdate(detachedUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ScheduleItem scheduleItem = getScheduleItem(scheduleId);

        // 구독 상태 검증
        validateSubscription(user);

        // 스케줄 상태 검증 (WAITING 또는 PLAYING만 입장 가능)
        validateScheduleStatus(scheduleItem);

        // 현재 입장 중인 기록이 있다면 재사용 (중복 생성 방지)
        var activeHistories = watchHistoryRepository
                .findAllByUserAndScheduleItemAndLeftAtIsNullOrderByCreatedAtDesc(user, scheduleItem);
        if (!activeHistories.isEmpty()) {
            WatchHistory current = activeHistories.get(0);
            // 중복 활성 기록이 있으면 모두 종료 처리
            if (activeHistories.size() > 1) {
                for (int i = 1; i < activeHistories.size(); i++) {
                    activeHistories.get(i).leave();
                }
            }
            current.reEnter();
            current.enter();
            watchHistoryRepository.saveAll(activeHistories);
            log.info("이미 입장 중인 기록 재사용: userId={}, scheduleId={}", user.getUserId(), scheduleId);
            return TheaterEnterResponse.from(current);
        }

        // 과거 기록이 있는 경우 (가장 최근 기록 사용)
        if (watchHistoryRepository.existsByUserAndScheduleItem(user, scheduleItem)) {
            WatchHistory history = watchHistoryRepository.findTopByUserAndScheduleItemOrderByCreatedAtDesc(user, scheduleItem)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 유저가 없습니다"));
            history.reEnter();
            history.enter();
            log.info("기존 시청 기록으로 재입장: userId={}, scheduleId={}", user.getUserId(), scheduleId);
            watchHistoryRepository.saveAndFlush(history);

            return TheaterEnterResponse.from(history);
        }

//        // 이미 입장한 경우 (퇴장하지 않은 기록이 있으면)
//        if (watchHistoryRepository.existsByUserAndScheduleItemAndLeftAtIsNull(user, scheduleItem)) {
//
//            var history = watchHistoryRepository.findByUserAndScheduleItem(user, scheduleItem)
//                    .orElseThrow(() -> new IllegalArgumentException("해당하는 유저가 없습니다."));
//
//            history.reEnter();
//            history.enter();
//
//            return TheaterEnterResponse.from(history);
//        }
        // 시청 기록 생성
        WatchHistory history = watchHistoryRepository.saveAndFlush(WatchHistory.create(user, scheduleItem));
        history.enter();
        log.info("새 시청 기록 생성: userId={}, scheduleId={}", user.getUserId(), scheduleId);
        return TheaterEnterResponse.from(history);
    }

    /**
     * 상영관 퇴장
     */
    @Transactional
    public TheaterLeaveResponse leave(long scheduleId, User detachedUser) {
        // 영속성 컨텍스트 내에서 User 다시 조회
        User user = userRepository.findById(detachedUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ScheduleItem scheduleItem = getScheduleItem(scheduleId);

        // 입장 기록 조회 (퇴장하지 않은 기록)
        Optional<WatchHistory> historyOpt = watchHistoryRepository
                .findByUserAndScheduleItemAndLeftAtIsNull(user, scheduleItem);

        // 입장 기록이 없으면 gracefully 처리
        if (historyOpt.isEmpty()) {
            log.warn("퇴장 요청했으나 입장 기록 없음: userId={}, scheduleId={}", user.getUserId(), scheduleId);
            return TheaterLeaveResponse.empty();
        }

        WatchHistory history = historyOpt.get();

        // 퇴장 처리
        history.leave();
        if (!history.getViewCounted() && history.isDiffMoreThanTenMinutes()) {
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
