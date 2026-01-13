package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(nullable = false)
    private Long amount;

    // [추가 1] 토스 API 요청 필수값 (주문 ID) - Unique Index 권장
   @Column(name = "order_id", nullable = false, unique = true)
   private String orderId;

    // [추가 2] 토스 API 요청 필수값 (주문명) - 예: "베이직 요금제 정기결제"
   @Column(name = "order_name", nullable = false)
   private String orderName;

    // [추가 3] 결제 실패 시 토스 응답 객체(failure)의 message 저장용
   @Column(name = "fail_reason")
   private String failReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
