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

    /**
     * 토스 빌링 응답과 유저 정보를 바탕으로 BillingKey 엔티티를 생성하는 정적 팩토리 메서드
     */
    public static BillingKey of(User user, com.example.cinema.infrastructure.payment.toss.dto.TossBillingResponse response) {
        return BillingKey.builder()
                .user(user)
                .provider(com.example.cinema.type.BillingProvider.TOSS)
                .billingKey(response.getBillingKey())
                .customerKey(user.getCustomerKey())
                .cardNumber(response.getCard().getNumber())
                .cardCompany(response.getCard().getIssuerCode())
                .cardType(response.getCard().getCardType())
                .ownerType(response.getCard().getOwnerType())
                .authenticatedAt(LocalDateTime.now())
                .status(com.example.cinema.type.BillingKeyStatus.ACTIVE)
                .build();
    }

    /**
     * 빌링키를 비활성화(REVOKED) 상태로 변경한 새로운 객체를 반환합니다.
     */
    public BillingKey revoke() {
        return BillingKey.builder()
                .billingKeyId(this.billingKeyId)
                .user(this.user)
                .provider(this.provider)
                .billingKey(this.billingKey)
                .customerKey(this.customerKey)
                .cardNumber(this.cardNumber)
                .cardCompany(this.cardCompany)
                .cardType(this.cardType)
                .ownerType(this.ownerType)
                .authenticatedAt(this.authenticatedAt)
                .status(com.example.cinema.type.BillingKeyStatus.REVOKED)
                .revokedAt(LocalDateTime.now())
                .build();
    }
}
