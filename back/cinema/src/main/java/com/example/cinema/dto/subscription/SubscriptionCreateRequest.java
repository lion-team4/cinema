package com.example.cinema.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신규 구독 신청 요청 DTO
 * <p>
 * 용도:
 * - 크리에이터 구독 및 정기 결제 등록 (POST /users/subscriptions)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionCreateRequest {

    /**
     * 구독할 크리에이터 ID
     */
    @NotNull(message = "Creator ID is required")
    private Long creatorId;

    /**
     * 구독 플랜 ID
     */
    @NotNull(message = "Plan ID is required")
    private Long planId;

    /**
     * PG사(프론트엔드 SDK)로부터 발급받은 1회성 인증 키
     */
    @NotBlank(message = "Auth key is required")
    private String authKey;
}