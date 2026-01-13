package com.example.cinema.dto.subscription;

import com.example.cinema.entity.Payment;
import com.example.cinema.type.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 내역 응답 DTO
 * <p>
 * 용도:
 * - 유저 구독 결제 기록 조회 (GET /users/subscriptions)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentHistoryResponse {
    private final Long paymentId;
    private final Long subscriptionId;
    private final String creatorNickname;
    private final Long amount;
    private final PaymentStatus status;
    private final LocalDateTime paidAt;

    public static PaymentHistoryResponse from(Payment payment) {
        return new PaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getSubscription().getSubscriptionId(),
                payment.getSubscription().getCreator().getNickname(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}
