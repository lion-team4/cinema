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

    Optional<WatchHistory> findByUserAndScheduleItem(User user,ScheduleItem scheduleItem);

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
     * 리뷰 작성 가능한 시청 기록 조회 (viewCounted=true, 아직 리뷰 작성 안 함)
     */
    @Query("SELECT wh FROM WatchHistory wh " +
            "LEFT JOIN Review r ON r.watchHistory = wh " +
            "WHERE wh.user = :user " +
            "AND wh.scheduleItem.content = :content " +
            "AND wh.viewCounted = true " +
            "AND r.reviewId IS NULL " +
            "ORDER BY wh.createdAt ASC")
    List<WatchHistory> findUsableWatchHistories(@Param("user") User user, @Param("content") Content content, Pageable pageable);

    boolean existsFindByUserAndScheduleItem(User user, ScheduleItem scheduleItem);

}
