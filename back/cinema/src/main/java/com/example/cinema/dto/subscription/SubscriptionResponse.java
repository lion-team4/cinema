package com.example.cinema.dto.subscription;

import com.example.cinema.entity.Subscription;
import com.example.cinema.type.SubscriptionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 구독 정보 응답 DTO
 * <p>
 * 용도:
 * - 내 구독 정보 조회 (GET /users/subscription)
 * - 결제 상태 및 다음 결제일 확인
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionResponse {
    private final Long subscriptionId;
    private final String creatorNickname;
    private final String planName;
    private final Long price;
    private final SubscriptionStatus status;
    /**
     * 다음 결제 예정일
     */
    private final LocalDateTime nextPaymentDate;

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getName(),
                subscription.getPrice(),
                subscription.getStatus(),
                subscription.getCurrentPeriodEnd()
        );
    }
}
