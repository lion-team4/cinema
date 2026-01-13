package com.example.cinema.service.Impl;

import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.payment.toss.TossBillingResponse;
import com.example.cinema.dto.payment.toss.TossPaymentResponse;
import com.example.cinema.dto.subscription.PaymentHistoryResponse;
import com.example.cinema.dto.subscription.SubscriptionCreateRequest;
import com.example.cinema.dto.subscription.SubscriptionListResponse;
import com.example.cinema.dto.subscription.SubscriptionUpdateBillingRequest;
import com.example.cinema.entity.BillingKey;
import com.example.cinema.entity.Payment;
import com.example.cinema.entity.Subscription;
import com.example.cinema.entity.User;
import com.example.cinema.infrastructure.payment.toss.TossPaymentClient;
import com.example.cinema.repository.BillingKeyRepository;
import com.example.cinema.repository.PaymentRepository;
import com.example.cinema.repository.SubscriptionRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.SubscriptionService;
import com.example.cinema.type.BillingKeyStatus;
import com.example.cinema.type.BillingProvider;
import com.example.cinema.type.PaymentStatus;
import com.example.cinema.type.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TossPaymentClient tossPaymentClient;
    
    // 단일 플랜 정보 (플랫폼은 단일 플랜만 제공)
    private static final String DEFAULT_PLAN_NAME = "기본 플랜";
    private static final Long DEFAULT_PLAN_PRICE = 10000L;
    private static final String DEFAULT_BILLING_CYCLE = "MONTHLY";

    @Override
    public SubscriptionListResponse createSubscription(Long userId, SubscriptionCreateRequest request) {
        // 이미 구독 중인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (subscriptionRepository.existsBySubscriber(user)) {
            throw new IllegalStateException("이미 구독 중입니다.");
        }

        // 고객 키 생성 또는 조회
        String customerKey = generateCustomerKey(user);

        // 빌링키 발급
        TossBillingResponse billingResponse = tossPaymentClient.issueBillingKey(
                request.getAuthKey(),
                customerKey
        );

        // 빌링키 저장
        BillingKey billingKey = BillingKey.builder()
                .user(user)
                .provider(BillingProvider.TOSS)
                .billingKey(billingResponse.getBillingKey())
                .customerKey(customerKey)
                .cardNumber(billingResponse.getCard().getNumber())
                .cardCompany(billingResponse.getCard().getIssuerCode())
                .cardType(billingResponse.getCard().getCardType())
                .ownerType(billingResponse.getCard().getOwnerType())
                .authenticatedAt(LocalDateTime.now())
                .status(BillingKeyStatus.ACTIVE)
                .build();

        billingKey = billingKeyRepository.save(billingKey);

        // 구독 생성 (단일 플랜 정보 직접 저장)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = now.plusMonths(1); // 월간 구독

        Subscription subscription = Subscription.builder()
                .subscriber(user)
                .planName(DEFAULT_PLAN_NAME)
                .price(DEFAULT_PLAN_PRICE)
                .billingCycle(DEFAULT_BILLING_CYCLE)
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(now)
                .currentPeriodEnd(periodEnd)
                .billingKey(billingKey)
                .build();

        subscription = subscriptionRepository.save(subscription);

        // 초기 결제 처리
        processInitialPayment(subscription);

        return SubscriptionListResponse.from(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionListResponse getMySubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        return SubscriptionListResponse.from(subscription);
    }

    @Override
    @Transactional(readOnly = true)
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
                        subscription, startDate, endDate, pageable)
                : paymentRepository.findBySubscription(subscription, pageable);

        return PageResponse.from(
                paymentPage.map(PaymentHistoryResponse::from)
        );
    }

    @Override
    public SubscriptionListResponse updateBillingKey(Long userId, SubscriptionUpdateBillingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        // 기존 빌링키 비활성화
        if (subscription.getBillingKey() != null) {
            BillingKey oldBillingKey = subscription.getBillingKey();
            oldBillingKey = billingKeyRepository.findById(oldBillingKey.getBillingKeyId())
                    .orElseThrow();
            oldBillingKey = BillingKey.builder()
                    .billingKeyId(oldBillingKey.getBillingKeyId())
                    .user(oldBillingKey.getUser())
                    .provider(oldBillingKey.getProvider())
                    .billingKey(oldBillingKey.getBillingKey())
                    .customerKey(oldBillingKey.getCustomerKey())
                    .cardNumber(oldBillingKey.getCardNumber())
                    .cardCompany(oldBillingKey.getCardCompany())
                    .cardType(oldBillingKey.getCardType())
                    .ownerType(oldBillingKey.getOwnerType())
                    .authenticatedAt(oldBillingKey.getAuthenticatedAt())
                    .status(BillingKeyStatus.REVOKED)
                    .revokedAt(LocalDateTime.now())
                    .build();
            billingKeyRepository.save(oldBillingKey);
        }

        // 새 빌링키 발급
        String customerKey = generateCustomerKey(user);
        TossBillingResponse billingResponse = tossPaymentClient.issueBillingKey(
                request.getAuthKey(),
                customerKey
        );

        // 새 빌링키 저장
        BillingKey newBillingKey = BillingKey.builder()
                .user(user)
                .provider(BillingProvider.TOSS)
                .billingKey(billingResponse.getBillingKey())
                .customerKey(customerKey)
                .cardNumber(billingResponse.getCard().getNumber())
                .cardCompany(billingResponse.getCard().getIssuerCode())
                .cardType(billingResponse.getCard().getCardType())
                .ownerType(billingResponse.getCard().getOwnerType())
                .authenticatedAt(LocalDateTime.now())
                .status(BillingKeyStatus.ACTIVE)
                .build();

        newBillingKey = billingKeyRepository.save(newBillingKey);

        // 구독에 새 빌링키 연결
        subscription = Subscription.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .subscriber(subscription.getSubscriber())
                .planName(subscription.getPlanName())
                .price(subscription.getPrice())
                .billingCycle(subscription.getBillingCycle())
                .status(subscription.getStatus())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .billingKey(newBillingKey)
                .build();

        subscription = subscriptionRepository.save(subscription);

        return SubscriptionListResponse.from(subscription);
    }

    @Override
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new IllegalStateException("구독 정보를 찾을 수 없습니다."));

        subscription = Subscription.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .subscriber(subscription.getSubscriber())
                .planName(subscription.getPlanName())
                .price(subscription.getPrice())
                .billingCycle(subscription.getBillingCycle())
                .status(SubscriptionStatus.CANCELED)
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .billingKey(subscription.getBillingKey())
                .build();

        subscriptionRepository.save(subscription);

        // 빌링키 해지
        if (subscription.getBillingKey() != null) {
            BillingKey billingKey = subscription.getBillingKey();
            billingKey = BillingKey.builder()
                    .billingKeyId(billingKey.getBillingKeyId())
                    .user(billingKey.getUser())
                    .provider(billingKey.getProvider())
                    .billingKey(billingKey.getBillingKey())
                    .customerKey(billingKey.getCustomerKey())
                    .cardNumber(billingKey.getCardNumber())
                    .cardCompany(billingKey.getCardCompany())
                    .cardType(billingKey.getCardType())
                    .ownerType(billingKey.getOwnerType())
                    .authenticatedAt(billingKey.getAuthenticatedAt())
                    .status(BillingKeyStatus.REVOKED)
                    .revokedAt(LocalDateTime.now())
                    .build();
            billingKeyRepository.save(billingKey);
        }
    }

    @Override
    public void processRecurringPayment(Subscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            return;
        }

        if (subscription.getBillingKey() == null) {
            return;
        }

        // 정기 결제 처리
        String orderId = "ORDER_" + UUID.randomUUID().toString().replace("-", "");
        String orderName = subscription.getPlanName() + " 정기결제";

        TossPaymentResponse paymentResponse = tossPaymentClient.requestPayment(
                subscription.getBillingKey().getBillingKey(),
                subscription.getBillingKey().getCustomerKey(),
                orderId,
                orderName,
                subscription.getPrice()
        );

        // 결제 이력 저장
        Payment payment = Payment.builder()
                .subscription(subscription)
                .providerPaymentId(paymentResponse.getPaymentKey())
                .amount(paymentResponse.getTotalAmount())
                .status(paymentResponse.getStatus().equals("DONE") 
                        ? PaymentStatus.APPROVED 
                        : PaymentStatus.FAILED)
                .paidAt(paymentResponse.getApprovedAt() != null 
                        ? parseDateTime(paymentResponse.getApprovedAt())
                        : LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // 결제 성공 시 구독 기간 연장
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            LocalDateTime now = LocalDateTime.now();
            subscription = Subscription.builder()
                    .subscriptionId(subscription.getSubscriptionId())
                    .subscriber(subscription.getSubscriber())
                    .planName(subscription.getPlanName())
                    .price(subscription.getPrice())
                    .billingCycle(subscription.getBillingCycle())
                    .status(SubscriptionStatus.ACTIVE)
                    .currentPeriodStart(now)
                    .currentPeriodEnd(now.plusMonths(1))
                    .billingKey(subscription.getBillingKey())
                    .build();
            subscriptionRepository.save(subscription);
        } else {
            // 결제 실패 시 구독 상태 변경
            subscription = Subscription.builder()
                    .subscriptionId(subscription.getSubscriptionId())
                    .subscriber(subscription.getSubscriber())
                    .planName(subscription.getPlanName())
                    .price(subscription.getPrice())
                    .billingCycle(subscription.getBillingCycle())
                    .status(SubscriptionStatus.EXPIRED)
                    .currentPeriodStart(subscription.getCurrentPeriodStart())
                    .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                    .billingKey(subscription.getBillingKey())
                    .build();
            subscriptionRepository.save(subscription);
        }
    }

    /**
     * 초기 결제 처리
     */
    private void processInitialPayment(Subscription subscription) {
        String orderId = "ORDER_" + UUID.randomUUID().toString().replace("-", "");
        String orderName = subscription.getPlanName() + " 초기결제";

        TossPaymentResponse paymentResponse = tossPaymentClient.requestPayment(
                subscription.getBillingKey().getBillingKey(),
                subscription.getBillingKey().getCustomerKey(),
                orderId,
                orderName,
                subscription.getPrice()
        );

        Payment payment = Payment.builder()
                .subscription(subscription)
                .providerPaymentId(paymentResponse.getPaymentKey())
                .amount(paymentResponse.getTotalAmount())
                .status(paymentResponse.getStatus().equals("DONE") 
                        ? PaymentStatus.APPROVED 
                        : PaymentStatus.FAILED)
                .paidAt(paymentResponse.getApprovedAt() != null 
                        ? parseDateTime(paymentResponse.getApprovedAt())
                        : LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // 결제 실패 시 구독 취소
        if (payment.getStatus() == PaymentStatus.FAILED) {
            cancelSubscription(subscription.getSubscriber().getUserId());
            throw new IllegalStateException("초기 결제에 실패했습니다.");
        }
    }

    /**
     * 고객 키 생성 (사용자별 고유 키)
     */
    private String generateCustomerKey(User user) {
        return "CUSTOMER_" + user.getUserId();
    }

    /**
     * 날짜 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // ISO 8601 형식: "2024-01-01T12:00:00+09:00" 또는 "2024-01-01T12:00:00"
            if (dateTimeStr.contains("T")) {
                String cleaned = dateTimeStr.split("\\+")[0].split("Z")[0];
                if (cleaned.length() > 19) {
                    cleaned = cleaned.substring(0, 19);
                }
                // "2024-01-01T12:00:00" 형식을 "2024-01-01 12:00:00"로 변환
                String formatted = cleaned.replace("T", " ");
                java.time.format.DateTimeFormatter formatter = 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(formatted, formatter);
            }
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}

