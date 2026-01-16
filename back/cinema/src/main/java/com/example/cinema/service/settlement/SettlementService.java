package com.example.cinema.service.settlement;

import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.settlement.SettlementExecutionResponse;
import com.example.cinema.dto.settlement.SettlementListResponse;
import com.example.cinema.dto.settlement.SettlementStatsResponse;
import com.example.cinema.entity.Settlement;
import com.example.cinema.entity.User;
import com.example.cinema.repository.settlement.SettlementRepository;
import com.example.cinema.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 정산 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {
    
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    
    /**
     * 정산 내역 조회
     */
    public PageResponse<SettlementListResponse> getSettlements(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Page<Settlement> settlements;
        if (startDate != null && endDate != null) {
            settlements = settlementRepository.findByCreatorAndPeriodStartBetween(
                user, startDate, endDate, pageable
            );
        } else {
            settlements = settlementRepository.findByCreator(user, pageable);
        }
        
        return PageResponse.from(
            settlements.map(SettlementListResponse::from)
        );
    }
    
    /**
     * 정산 통계 조회
     */
    public SettlementStatsResponse getSettlementStats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Long totalSettlementAmount = settlementRepository.sumAmountByCreator(user);
        if (totalSettlementAmount == null) {
            totalSettlementAmount = 0L;
        }
        
        long totalSettlements = settlementRepository.countByCreator(user);
        
        Settlement lastSettlement = settlementRepository
            .findFirstByCreatorOrderByPeriodStartDesc(user)
            .orElse(null);
        
        LocalDate lastSettlementDate = lastSettlement != null 
            ? lastSettlement.getPeriodStart() 
            : null;
        Long lastSettlementAmount = lastSettlement != null 
            ? lastSettlement.getAmount() 
            : 0L;
        
        return new SettlementStatsResponse(
            totalSettlementAmount,
            totalSettlements,
            lastSettlementDate,
            lastSettlementAmount
        );
    }
    
    /**
     * 정산 수동 실행 (관리자용)
     */
    @Transactional
    public SettlementExecutionResponse executeSettlement(
            LocalDate periodStart,
            LocalDate periodEnd) {
        
        try {
            LocalDateTime startDateTime = periodStart.atStartOfDay();
            LocalDateTime endDateTime = periodEnd.atTime(23, 59, 59);
            
            // Job 파라미터 설정
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("periodStart", periodStart.format(DateTimeFormatter.ISO_DATE))
                .addString("periodEnd", periodEnd.format(DateTimeFormatter.ISO_DATE))
                .addString("startDateTime", startDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .addString("endDateTime", endDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            // Job 실행
            log.info("정산 Job 수동 실행: period={}~{}", periodStart, periodEnd);
            var jobExecution = jobLauncher.run(settlementJob, jobParameters);
            
            // 정산 금액 집계
            Long totalSettlementAmount = settlementRepository.sumAmountByPeriod(periodStart, periodEnd);
            if (totalSettlementAmount == null) {
                totalSettlementAmount = 0L;
            }
            
            // 처리된 정산 건수
            long processedSettlements = settlementRepository.countByPeriodStartBetween(periodStart, periodEnd);
            
            return new SettlementExecutionResponse(
                periodStart,
                periodEnd,
                totalSettlementAmount,
                (int) processedSettlements,
                jobExecution.getStatus().toString()
            );
        } catch (Exception e) {
            log.error("정산 Job 실행 실패", e);
            throw new RuntimeException("정산 Job 실행 실패: " + e.getMessage(), e);
        }
    }
}

