package com.example.cinema.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Content Owner별 조회수 집계 DTO (Batch 처리용)
 */
@Getter
@AllArgsConstructor
public class ContentOwnerViewSummary {
    private Long ownerId;           // 크리에이터 User ID
    private String ownerEmail;      // 크리에이터 이메일
    private String ownerNickname;   // 크리에이터 닉네임
    private Long totalViews;        // 해당 Owner의 총 조회수 (monthView >= 10인 Content만 합산)
}

