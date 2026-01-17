package com.example.cinema.repository.schedule;

import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.repository.schedule.custom.ScheduleItemRepositoryCustom;
import com.example.cinema.type.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> , ScheduleItemRepositoryCustom {

    /**
     * 특정 창작자(Owner)의 다른 상영 일정과 시간이 겹치는지 확인합니다.
     * 시간 겹침 판별 로직: (기존 시작 < 요청 종료) AND (기존 종료 > 요청 시작)
     * @param ownerId 창작자 ID
     * @param startAt 요청 시작 시간
     * @param endAt 요청 종료 시간
     * @param excludeId 수정 시 자기 자신을 제외하기 위한 ID (생성 시 null)
     * @return 겹치는 일정이 존재하면 true
     */
    @Query("""
    SELECT COUNT(s) > 0 
    FROM ScheduleItem s 
    WHERE s.content.owner.userId = :ownerId 
      AND (:excludeId IS NULL OR s.scheduleItemId <> :excludeId)
      AND (s.startAt < :endAt AND s.endAt > :startAt)
""")
    boolean existsOverlapByOwner(@Param("ownerId") Long ownerId,
                                 @Param("startAt") LocalDateTime startAt,
                                 @Param("endAt") LocalDateTime endAt,
                                 @Param("excludeId") Long excludeId);
    java.util.List<ScheduleItem> findAllByScheduleDay_ScheduleDayId(Long scheduleDayId);

    // 오픈: CLOSED -> WAITING (startAt-10분 구간)
    // 조건: startAt > now && startAt <= now+10분
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ScheduleItem s
           set s.status = com.example.cinema.type.ScheduleStatus.WAITING
         where s.status = com.example.cinema.type.ScheduleStatus.CLOSED
           and s.startAt > :now
           and s.startAt <= :nowPlus10
    """)
    int closedToWaiting(@Param("now") LocalDateTime now,
                        @Param("nowPlus10") LocalDateTime nowPlus10);


    @Query("""
        select s.scheduleItemId
        from ScheduleItem s
        where s.status = com.example.cinema.type.ScheduleStatus.WAITING
          and s.startAt <= :now
    """)
    List<Long> findIdsWaitingToPlaying(@Param("now") LocalDateTime now);

    @Query("""
        select s.scheduleItemId
        from ScheduleItem s
        where s.status = com.example.cinema.type.ScheduleStatus.PLAYING
          and s.endAt <= :now
    """)
    List<Long> findIdsPlayingToEnding(@Param("now") LocalDateTime now);

    @Query("""
        select s.scheduleItemId
        from ScheduleItem s
        where s.status = com.example.cinema.type.ScheduleStatus.ENDING
          and s.endAt <= :nowMinus10
    """)
    List<Long> findIdsEndingToClosed(@Param("nowMinus10") LocalDateTime nowMinus10);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ScheduleItem s
           set s.status = :to
         where s.scheduleItemId in :ids
    """)
    int updateStatusByIds(@Param("to") ScheduleStatus to,
                          @Param("ids") List<Long> ids);
}


