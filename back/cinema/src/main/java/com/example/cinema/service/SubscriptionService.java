package com.example.cinema.service;

import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.subscription.PaymentHistoryResponse;
import com.example.cinema.dto.subscription.SubscriptionCreateRequest;
import com.example.cinema.dto.subscription.SubscriptionListResponse;
import com.example.cinema.dto.subscription.SubscriptionUpdateBillingRequest;
import com.example.cinema.entity.Subscription;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface SubscriptionService {
    /**
     * 플랫폼 구독 생성 및 초기 결제 처리
     */
    SubscriptionListResponse createSubscription(Long userId, SubscriptionCreateRequest request);

    /**
     * 내 구독 정보 조회
     */
    SubscriptionListResponse getMySubscription(Long userId);

    /**
     * 구독 결제 내역 조회
     */
    PageResponse<PaymentHistoryResponse> getPaymentHistory(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 결제 수단(빌링키) 변경
     */
    SubscriptionListResponse updateBillingKey(Long userId, SubscriptionUpdateBillingRequest request);

    /**
     * 구독 해지
     */
    void cancelSubscription(Long userId);

    /**
     * 정기 결제 처리 (스케줄러에서 호출)
     */
    void processRecurringPayment(Subscription subscription);
}

