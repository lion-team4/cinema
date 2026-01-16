package com.example.cinema.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 정산 통계 응답 DTO
 */
@Getter
@AllArgsConstructor
public class SettlementStatsResponse {
    private Long totalSettlementAmount;  // 총 정산 금액
    private Long totalSettlements;       // 총 정산 건수
    private LocalDate lastSettlementDate; // 마지막 정산일
    private Long lastSettlementAmount;   // 마지막 정산 금액
}

