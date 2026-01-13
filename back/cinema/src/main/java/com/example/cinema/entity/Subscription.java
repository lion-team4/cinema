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
@Table(name = "subscriptions") // 유저의 서비스(플랫폼) 구독 정보
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_user_id", nullable = false)
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

    public void cancel() {
        this.isActive = false;
        this.status = SubscriptionStatus.CANCELED;
    }
}
