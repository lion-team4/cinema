package com.example.cinema.scheduler.test;

import com.example.cinema.entity.Subscription;
import com.example.cinema.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class TestSubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    // 30초에 한번씩
    @Scheduled(fixedDelay = 30000)
    public void scheduleRecurringPayment() {
        log.info("=== Starting Recurring Payment Schedule Test ===");

        // 1. 대상 조회 (Read-Only)
        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionForTest();
        log.info("Found {} subscriptions to process.", subscriptions.size());

        // 2. 병렬 처리 (Parallel Stream)
        subscriptions.parallelStream()
                .map(Subscription::getSubscriptionId) // ID만 추출하여 전달 (Thread-Safe)
                .forEach(subId -> {
                    try {
                        // 3. 개별 결제 로직 호출 (Transaction Boundary: REQUIRES_NEW)
                        subscriptionService.processRecurringPayment(subId);
                    } catch (Exception e) {
                        log.error("Failed to trigger payment for subId: {}", subId, e);
                    }
                });

        log.info("=== Schedule Completed (Processed asynchronously) ===");
    }
}
