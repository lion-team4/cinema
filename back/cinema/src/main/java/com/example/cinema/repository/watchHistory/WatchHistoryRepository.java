package com.example.cinema.repository.watchHistory;

import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.entity.User;
import com.example.cinema.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    /**
     * 특정 사용자의 특정 스케줄 시청 기록 조회 (퇴장 안 한 기록)
     */
    Optional<WatchHistory> findByUserAndScheduleItemAndLeftAtIsNull(User user, ScheduleItem scheduleItem);

    /**
     * 특정 사용자의 특정 스케줄 시청 기록 존재 여부 (퇴장 안 한 기록)
     */
    boolean existsByUserAndScheduleItemAndLeftAtIsNull(User user, ScheduleItem scheduleItem);

    /**
     * 특정 사용자의 전체 시청 기록 조회
     */
    List<WatchHistory> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 스케줄의 현재 시청 중인 사용자 수
     */
    long countByScheduleItemAndLeftAtIsNull(ScheduleItem scheduleItem);

    /**
     * 조회수 카운트 대상 조회
     * - 현재 입장 중 (leftAt IS NULL)
     * - 아직 카운트 안 됨 (viewCounted = false)
     * - 1시간 이상 시청 (createdAt <= threshold)
     */
    @Query("""
        SELECT wh FROM WatchHistory wh
        JOIN FETCH wh.scheduleItem si
        JOIN FETCH si.content c
        WHERE wh.leftAt IS NULL
          AND wh.viewCounted = false
          AND wh.createdAt <= :threshold
        """)
    List<WatchHistory> findEligibleForViewCount(@Param("threshold") LocalDateTime threshold);
}
