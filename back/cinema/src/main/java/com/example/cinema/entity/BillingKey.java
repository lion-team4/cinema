package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.BillingKeyStatus;
import com.example.cinema.type.BillingProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "billing_keys")
public class BillingKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_key_id")
    private Long billingKeyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingProvider provider;

    @Column(name = "billing_key", nullable = false, unique = true)
    private String billingKey;

    @Column(name = "card_last4")
    private String cardLast4;

    @Column(name = "card_brand")
    private String cardBrand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingKeyStatus status;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}
