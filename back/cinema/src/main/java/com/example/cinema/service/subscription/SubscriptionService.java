package com.example.cinema.service.subscription;

import com.example.cinema.dto.billing.BillingResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.subscription.*;
import com.example.cinema.infrastructure.payment.toss.dto.TossBillingResponse;
import com.example.cinema.infrastructure.payment.toss.dto.TossPaymentResponse;
import com.example.cinema.entity.BillingKey;
import com.example.cinema.entity.Payment;
import com.example.cinema.entity.Subscription;
import com.example.cinema.entity.User;
import com.example.cinema.infrastructure.payment.toss.TossPaymentClient;
import com.example.cinema.repository.BillingKeyRepository;
import com.example.cinema.repository.PaymentRepository;
import com.example.cinema.repository.SubscriptionRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.BillingKeyStatus;
import com.example.cinema.type.BillingProvider;
import com.example.cinema.type.PaymentStatus;
import com.example.cinema.type.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TossPaymentClient tossPaymentClient;


    // 구독 생성 및 초기 결제
    @Transactional
    public FirstSubscriptionResponse createSubscription(Long userId, SubscriptionCreateRequest request) {
        // 1. 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. 이미 구독 중인지 확인
        if (subscriptionRepository.existsBySubscriber(user)) {
            Subscription existing = subscriptionRepository.findBySubscriber(user).get();
            if (existing.getStatus() == SubscriptionStatus.ACTIVE) {
                 throw new IllegalStateException("이미 활성화된 구독이 있습니다.");
            }
            // 만약 해지된(CANCELED, EXPIRED) 구독이라면 아래 로직 진행 (새로 생성하거나 재활성화)
            // 여기서는 심플하게 '구독 레코드가 있으면 에러' 처리하거나, 정책에 따라 재가입 허용 가능.
            // 일단 '이미 있으면 불가'로 처리하고, 재가입은 별도 로직이 필요할 수 있음.
             throw new IllegalStateException("이미 구독 정보가 존재합니다. (해지 후 재가입은 추후 지원)");
        }

        // 3. 고객 키 가져오기 (User 엔티티 위임)
        String customerKey = user.getCustomerKey();

        // 4. Toss API 빌링키 발급 요청
        TossBillingResponse billingResponse = tossPaymentClient.issueBillingKey(
                request.getAuthKey(),
                customerKey
        );

        // 5. 빌링키 Entity 저장 (정적 팩토리 메서드 사용)
        BillingKey billingKey =
                billingKeyRepository.save(
                                BillingKey.of(user, billingResponse)
                        );

        log.info("Billing: {}", billingKey.getBillingKey());
        log.info("Customer: {}", billingKey.getCustomerKey());

        // 6. Subscription Entity 생성
        Subscription subscription =
                subscriptionRepository.save(
                        Subscription.create(user,billingKey)
                );

        // 7. 초기 결제 처리
        return processInitialPayment(subscription);
    }



    //구독 정보 조회
    public SubscriptionResponse getMySubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        return SubscriptionResponse.from(subscription);
    }



    // 결제 내역 조회
    public PageResponse<PaymentHistoryResponse> getPaymentHistory(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));


        var paymentPage = (startDate != null && endDate != null)
                ? paymentRepository.findBySubscriptionAndPaidAtBetween(
                        subscription, startDate, endDate, pageable
                )
                : paymentRepository.findBySubscription(subscription, pageable);

        return PageResponse.from(
                paymentPage.map(PaymentHistoryResponse::from)
        );
    }



    @Transactional
    public SubscriptionResponse updateBillingKey(Long userId, SubscriptionUpdateBillingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        // 기존 빌링키 비활성화
        if (subscription.getBillingKey() != null) {
            billingKeyRepository.save(subscription.getBillingKey().revoke());
        }

        // 새 빌링키 발급
        String customerKey = user.getCustomerKey();
        TossBillingResponse billingResponse = tossPaymentClient.issueBillingKey(
                request.getAuthKey(),
                customerKey
        );

        // 새 빌링키 저장
        BillingKey newBillingKey = billingKeyRepository.save(BillingKey.of(user, billingResponse));

        // 구독 정보 업데이트
        // Subscription 엔티티에 updateBillingKey 메서드가 있으면 좋겠지만, 여기서는 Builder로 처리
        subscription.updateBullingKey(newBillingKey);

        return SubscriptionResponse.from(subscription);
    }

    // 구독 취소
    @Transactional
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        // 구독 취소 (Entity 메서드 활용)
        subscription.cancel();
        subscriptionRepository.save(subscription);

        // 빌링키도 해지(REVOKED) 처리
        if (subscription.getBillingKey() != null) {
            billingKeyRepository.save(subscription.getBillingKey().revoke());
        }
    }

    public BillingResponse getBilling(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        return BillingResponse.from(subscription.getBillingKey());
    }

    // --- 내부 결제 로직 ---
    // 최초 결제
    private FirstSubscriptionResponse processInitialPayment(Subscription subscription) {
        String orderId = "ORDER_INIT_" + UUID.randomUUID().toString().substring(0, 18);
        String orderName = subscription.getName() + " (최초결제)";

        TossPaymentResponse paymentResponse = null;
        try {
            paymentResponse = tossPaymentClient.requestPayment(
                    subscription.getBillingKey().getBillingKey(),
                    subscription.getBillingKey().getCustomerKey(),
                    orderId,
                    orderName,
                    subscription.getPrice()
            );
        } catch (Exception e) {
            // 결제 요청 실패 시 구독 취소 처리 (Rollback 개념)
            // Transactional이므로 예외 던지면 DB 롤백됨.
            throw new RuntimeException("초기 결제 요청 중 오류가 발생했습니다: " + e.getMessage());
        }

        Payment payment = paymentRepository
                .save(Payment.create(subscription, paymentResponse));

        if (payment.getStatus() == PaymentStatus.FAILED) {
            // 결제 실패 시 예외를 던져 트랜잭션 롤백 (구독 생성 취소)
            throw new IllegalStateException("초기 결제 승인이 거절되었습니다.");
        }



        return FirstSubscriptionResponse.from(
                SubscriptionResponse.from(subscription),
                PaymentHistoryResponse.from(payment)
        );
    }


    //정기 결제 (스케줄러 등에서 호출)
    @Transactional
    public void processRecurringPayment(Subscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) return;
        if (subscription.getBillingKey() == null) return;

        String orderId = "ORDER_REC_" + UUID.randomUUID().toString().substring(0, 18);
        String orderName = subscription.getName() + " (정기결제)";

        try {
            TossPaymentResponse paymentResponse = tossPaymentClient.requestPayment(
                    subscription.getBillingKey().getBillingKey(),
                    subscription.getBillingKey().getCustomerKey(),
                    orderId,
                    orderName,
                    subscription.getPrice()
            );

            Payment payment = paymentRepository
                    .save(Payment.create(subscription, paymentResponse));

            if (payment.getStatus() == PaymentStatus.APPROVED) {
                // 기간 연장
                subscription.extensionPeriod();
                subscription.renew();

                subscriptionRepository.save(subscription);
            } else {
                // 결제 실패 시 -> 보류/해지 등 정책 처리 (여기선 만료 처리)
                subscription.cancel(); // 또는 EXPIRED
                subscriptionRepository.save(subscription);
            }

        } catch (Exception e) {
            // 시스템 오류 등으로 결제 실패
             subscription.cancel();
             subscriptionRepository.save(subscription);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr.contains("T")) {
                String cleaned = dateTimeStr.split("\\+")[0].split("Z")[0];
                if (cleaned.length() > 19) cleaned = cleaned.substring(0, 19);
                return LocalDateTime.parse(cleaned);
            }
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}