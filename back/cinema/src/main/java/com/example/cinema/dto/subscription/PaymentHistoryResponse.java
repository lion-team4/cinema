package com.example.cinema.dto.subscription;

import com.example.cinema.entity.Payment;
import com.example.cinema.type.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentHistoryResponse {
    private final Long paymentId;
    private final Long subscriptionId;
    private final String planName;
    private final Long amount;
    private final PaymentStatus status;
    private final LocalDateTime paidAt;

    public static PaymentHistoryResponse from(Payment payment) {
        return new PaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getSubscription().getSubscriptionId(),
                payment.getSubscription().getName(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}