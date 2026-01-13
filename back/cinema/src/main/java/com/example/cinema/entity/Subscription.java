package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_user_id", nullable = false, unique = true)
    private User subscriber;

    @Builder.Default
    private String name = "기본요금제";

    @Builder.Default
    private Long price = 10000L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_key_id")
    private BillingKey billingKey;

    public void cancel() { //구독 소
        this.isActive = false;
        this.status = SubscriptionStatus.CANCELED;
    }

    public void renew(){ //구독 재개
        this.isActive = true;
        this.status = SubscriptionStatus.ACTIVE;
    }


    public void expire(){ //구독 만료 or 결제 전 or 결제 실패
        this.isActive = false;
        this.status = SubscriptionStatus.EXPIRED;
    }

    public void updateBullingKey(BillingKey newBillingKey){
        this.billingKey = newBillingKey;
    }

    public void extensionPeriod(){
        currentPeriodStart = LocalDateTime.now();
        currentPeriodEnd = LocalDateTime.now().plusMonths(1);
    }

    // 구독 생성
    public static Subscription create(User user, BillingKey billingKey){
        return Subscription.builder()
                .subscriber(user)
                .status(SubscriptionStatus.ACTIVE)
                .isActive(true)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .billingKey(billingKey)
                .build();
    }

    public void reActivate(BillingKey newBillingKey) {
        this.isActive = true;
        this.status = SubscriptionStatus.ACTIVE;
        this.billingKey = newBillingKey;

        LocalDateTime now = LocalDateTime.now();

        // 1. 기존 만료일(currentPeriodEnd)이 현재보다 미래라면 (기간이 남았다면)
        if (this.currentPeriodEnd != null && this.currentPeriodEnd.isAfter(now)) {
            this.currentPeriodEnd = this.currentPeriodEnd.plusMonths(1);
        }
        // 2. 기존 만료일이 없거나, 이미 지났다면
        else {
            // 현재 시점부터 1개월 설정
            this.currentPeriodStart = now;
            this.currentPeriodEnd = now.plusMonths(1);
        }
    }
}
