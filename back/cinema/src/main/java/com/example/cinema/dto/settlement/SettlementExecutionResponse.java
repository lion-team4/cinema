package com.example.cinema.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 정산 실행 결과 응답 DTO
 */
@Getter
@AllArgsConstructor
public class SettlementExecutionResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Long totalSettlementAmount; // 총 정산 금액 (조회수 × 100원)
    private Integer processedSettlements; // 처리된 정산 건수
    private String jobExecutionStatus;
}

