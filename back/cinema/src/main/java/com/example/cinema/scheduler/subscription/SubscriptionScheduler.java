package com.example.cinema.scheduler.subscription;

import com.example.cinema.entity.Subscription;
import com.example.cinema.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    // 매일 오전 3시 (03:00:00) 실행
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduleRecurringPayment() {
        log.info("=== Starting Recurring Payment Schedule ===");

        // 1. 대상 조회 (Read-Only)
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> subscriptions = subscriptionService.findExpiredSubscriptions(now);

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
