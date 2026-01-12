package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(name = "billing_cycle")
    @Builder.Default
    private String billingCycle = "MONTHLY";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
