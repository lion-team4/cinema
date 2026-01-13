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
 * - 플랫폼 구독 및 정기 결제 등록 (POST /users/subscriptions)
 * - 단일 플랜만 제공하므로 planId는 불필요
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionCreateRequest {


    /**
     * PG사(프론트엔드 SDK)로부터 발급받은 1회성 인증 키
     */
    @NotBlank(message = "Auth key is required")
    private String authKey;
}