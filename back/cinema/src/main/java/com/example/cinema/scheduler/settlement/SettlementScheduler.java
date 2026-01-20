package com.example.cinema.scheduler.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 정산 스케줄러
 * 매월 1일 00:00:00에 전월 정산을 자동 실행
 * 
 * 테스트용: 1분마다 실행 (운영 환경에서는 주석 처리하고 원래 cron 표현식 사용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    
//     테스트용: 1분마다 실행
     @Scheduled(cron = "0 * * * * ?") // 매분 0초에 실행 (1분마다)
    // 운영용: 매월 1일 00:00:00 실행
//     @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 00:00:00
    public void executeMonthlySettlement() {
        try {
            // 전월 기간 계산
            LocalDate now = LocalDate.now();
            LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
            LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(
                now.minusMonths(1).lengthOfMonth()
            );
            
            LocalDateTime startDateTime = lastMonthStart.atStartOfDay();
            LocalDateTime endDateTime = lastMonthEnd.atTime(23, 59, 59);
            
            // Job 파라미터 설정
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("periodStart", lastMonthStart.format(DateTimeFormatter.ISO_DATE))
                .addString("periodEnd", lastMonthEnd.format(DateTimeFormatter.ISO_DATE))
                .addString("startDateTime", startDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .addString("endDateTime", endDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            // Job 실행
            log.info("정산 Job 실행 시작: period={}~{}", lastMonthStart, lastMonthEnd);
            jobLauncher.run(settlementJob, jobParameters);
            log.info("정산 Job 실행 완료: period={}~{}", lastMonthStart, lastMonthEnd);
        } catch (Exception e) {
            // 에러 로깅 및 알림 처리
            log.error("정산 Job 실행 실패", e);
        }
    }
}

