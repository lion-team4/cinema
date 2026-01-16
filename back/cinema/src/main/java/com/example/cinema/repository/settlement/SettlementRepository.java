package com.example.cinema.repository.settlement;

import com.example.cinema.entity.Settlement;
import com.example.cinema.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    
    // 크리에이터별 정산 내역 조회
    Page<Settlement> findByCreator(User creator, Pageable pageable);
    
    // 기간별 정산 내역 조회
    Page<Settlement> findByCreatorAndPeriodStartBetween(
        User creator,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );
    
    // 크리에이터의 총 정산 금액
    @Query("SELECT SUM(s.amount) FROM Settlement s WHERE s.creator = :creator")
    Long sumAmountByCreator(@Param("creator") User creator);
    
    // 멱등성 보장: 동일 기간/크리에이터로 정산 레코드 조회
    Optional<Settlement> findByCreatorAndPeriodStartAndPeriodEnd(
        User creator,
        LocalDate periodStart,
        LocalDate periodEnd
    );
    
    // 기간별 총 정산 금액 계산 (플랫폼 수입 계산용)
    @Query("SELECT SUM(s.amount) FROM Settlement s " +
           "WHERE s.periodStart >= :startDate AND s.periodEnd <= :endDate")
    Long sumAmountByPeriod(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // 전체 기간 총 정산 금액 계산 (플랫폼 수입 통계용)
    @Query("SELECT SUM(s.amount) FROM Settlement s")
    Long sumTotalAmount();
    
    // 크리에이터의 최근 정산 내역 조회
    Optional<Settlement> findFirstByCreatorOrderByPeriodStartDesc(User creator);
    
    // 크리에이터별 정산 건수
    long countByCreator(User creator);
    
    // 기간별 정산 건수
    long countByPeriodStartBetween(LocalDate startDate, LocalDate endDate);
    
    // 월별 정산 금액 집계 (DB에서 직접 GROUP BY)
    // 연-월별로 periodStart를 기준으로 그룹화하여 SUM(amount) 계산
    // MySQL의 YEAR(), MONTH() 함수를 네이티브 쿼리로 사용
    @Query(value = "SELECT " +
           "YEAR(s.period_start) as year, " +
           "MONTH(s.period_start) as month, " +
           "MIN(s.period_start) as periodStart, " +
           "MAX(s.period_end) as periodEnd, " +
           "SUM(s.amount) as totalSettlementAmount " +
           "FROM settlements s " +
           "WHERE (:startDate IS NULL OR s.period_start >= :startDate) " +
           "AND (:endDate IS NULL OR s.period_start <= :endDate) " +
           "GROUP BY YEAR(s.period_start), MONTH(s.period_start) " +
           "ORDER BY year DESC, month DESC",
           nativeQuery = true)
    List<Object[]> findMonthlySettlementSummary(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

