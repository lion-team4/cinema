package com.example.cinema.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 수단 변경 요청 DTO
 * <p>
 * 용도:
 * - 기존 구독의 결제 카드(빌링키) 변경 (PUT /users/subscriptions)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionUpdateBillingRequest {

    /**
     * 새로 발급받은 1회성 인증 키 (빌링키 교체용)
     */
    @NotBlank(message = "New auth key is required")
    private String authKey;
}