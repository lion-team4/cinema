package com.example.cinema.dto.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 플랫폼 수입 통계 응답 DTO
 */
@Getter
@AllArgsConstructor
public class PlatformRevenueStatsResponse {
    private Long totalPlatformRevenue;   // 전체 플랫폼 수입
    private Long totalPaymentAmount;      // 전체 구독 결제 금액
    private Long totalSettlementAmount;  // 전체 정산 금액
    private Long averageMonthlyRevenue;  // 월평균 플랫폼 수입
    private Long lastMonthRevenue;        // 전월 플랫폼 수입
    private LocalDate lastMonthDate;      // 전월 날짜
}

