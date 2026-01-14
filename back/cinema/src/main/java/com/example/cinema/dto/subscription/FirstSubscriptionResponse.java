package com.example.cinema.dto.subscription;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FirstSubscriptionResponse {
    private SubscriptionResponse subscription;
    private PaymentHistoryResponse payment;

    public static FirstSubscriptionResponse from(SubscriptionResponse subscription, PaymentHistoryResponse payment) {
        return new FirstSubscriptionResponse(subscription, payment);
    }
}
