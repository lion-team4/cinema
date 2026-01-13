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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingProvider provider;

    @Column(name = "billing_key", nullable = false, unique = true)
    private String billingKey;

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_company")
    private String cardCompany;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "owner_type")
    private String ownerType;

    @Column(name = "authenticated_at")
    private LocalDateTime authenticatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingKeyStatus status;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}