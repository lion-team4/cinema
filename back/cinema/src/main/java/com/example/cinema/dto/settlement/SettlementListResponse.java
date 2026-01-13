package com.example.cinema.dto.settlement;

import com.example.cinema.entity.Settlement;
import com.example.cinema.type.SettlementStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 정산 내역 목록 응답 DTO
 * <p>
 * 용도:
 * - 월별 정산 내역 및 지급 상태 조회 (GET /settlements)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementListResponse {
    private final Long settlementId;
    /**
     * 정산 대상 기간 시작일
     */
    private final LocalDate periodStart;
    /**
     * 정산 대상 기간 종료일
     */
    private final LocalDate periodEnd;
    /**
     * 기간 내 총 조회수 (정산 기준)
     */
    private final Long totalViews;
    /**
     * 정산 확정 금액
     */
    private final Long amount;
    /**
     * 정산 상태 (PROCESSING, COMPLETED 등)
     */
    private final SettlementStatus status;

    public static SettlementListResponse from(Settlement settlement) {
        return new SettlementListResponse(
                settlement.getSettlementId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getTotalViews(),
                settlement.getAmount(),
                settlement.getStatus()
        );
    }
}