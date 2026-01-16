package com.example.cinema.dto.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 플랫폼 수입 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
public class PlatformRevenueResponse {
    private LocalDate periodStart;        // 기간 시작일
    private LocalDate periodEnd;          // 기간 종료일
    private Long totalPaymentAmount;      // 총 구독 결제 금액
    private Long totalSettlementAmount;   // 총 정산 금액
    private Long platformRevenue;        // 플랫폼 수입
}

