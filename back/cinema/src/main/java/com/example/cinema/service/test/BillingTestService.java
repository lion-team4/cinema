package com.example.cinema.service.test;

import com.example.cinema.entity.*;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.infrastructure.payment.toss.TossPaymentClient;
import com.example.cinema.infrastructure.payment.toss.dto.TossBillingResponse;
import com.example.cinema.infrastructure.payment.toss.dto.TossPaymentResponse;
import com.example.cinema.repository.billing.BillingKeyRepository;
import com.example.cinema.repository.payment.PaymentRepository;
import com.example.cinema.repository.subscription.SubscriptionRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.BillingKeyStatus;
import com.example.cinema.type.BillingProvider;
import com.example.cinema.type.PaymentStatus;
import com.example.cinema.type.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Profile("dev")
@Slf4j
public class BillingTestService {

    private final UserRepository userRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;

        /**
     * 테스트용 사용자를 생성하거나 조회합니다.
     * @param nickname 사용자 닉네임 (customerKey로 사용)
     * @param email 사용자 이메일 (없을 시 자동 생성)
     * @return 생성 또는 조회된 User 엔티티
     */
    public User createTestUser(String nickname, String email) {
        return userRepository.findByNickname(nickname)
                .orElseGet(() -> userRepository.save(User.builder()
                        .nickname(nickname)
                        .email(email != null ? email : "test_" + nickname + "@cinema.com")
                        .passwordHash("test_password") // 테스트용 임시 비번
                        .seller(false)
                        .build()));
    }

        /**
     * 빌링키를 발급하고 DB에 저장합니다.
     * @param authKey 토스에서 전달된 인증 키
     * @param customerKey 사용자 닉네임
     * @return 토스 API 빌링키 발급 응답
     */
    public TossBillingResponse registerBillingKey(String authKey, String customerKey) {
        // 1. Toss API 호출
        TossBillingResponse response = tossPaymentClient.issueBillingKey(authKey, customerKey);

        // 2. User 조회 (customerKey = nickname)
        User user = userRepository.findByNickname(customerKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. BillingKey 저장
        BillingKey billingKey = BillingKey.builder()
                .user(user)
                .provider(BillingProvider.TOSS)
                .billingKey(response.getBillingKey())
                .customerKey(customerKey)
                .cardNumber(response.getCard().getNumber())
                .cardCompany(response.getCard().getIssuerCode())
                .cardType(response.getCard().getCardType())
                .ownerType(response.getCard().getOwnerType())
                .authenticatedAt(LocalDateTime.now())
                .status(BillingKeyStatus.ACTIVE)
                .build();
        billingKey = billingKeyRepository.save(billingKey);

        // 4. 테스트용 Subscription 생성 (결제 테스트를 위해 필요)
        if (!subscriptionRepository.existsBySubscriber(user)) {
            Subscription subscription = Subscription.builder()
                    .subscriber(user)
                    .billingKey(billingKey)
                    .name("테스트 정기구독")
                    .price(1000L)
                    .isActive(true)
                    .status(SubscriptionStatus.ACTIVE)
                    .currentPeriodStart(LocalDateTime.now())
                    .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                    .build();
            subscriptionRepository.save(subscription);
        }

        return response;
    }

        /**
     * 등록된 빌링키로 결제를 요청합니다.
     * @param customerKey 사용자 닉네임
     * @param amount 결제 금액
     * @return 토스 API 결제 승인 응답
     */
    public TossPaymentResponse pay(String customerKey, Long amount) {
        User user = userRepository.findByNickname(customerKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new BusinessException("구독 정보를 찾을 수 없습니다.", ErrorCode.ENTITY_NOT_FOUND));

        BillingKey billingKey = subscription.getBillingKey();
        if (billingKey == null || billingKey.getStatus() != BillingKeyStatus.ACTIVE) {
            throw new BusinessException("활성화된 빌링키가 없습니다.", ErrorCode.BILLING_KEY_NOT_FOUND);
        }

        String orderId = "TEST_ORDER_" + UUID.randomUUID().toString().substring(0, 8);
        String orderName = "테스트 결제 - " + amount + "원";

        // Toss API 호출
        TossPaymentResponse response = tossPaymentClient.requestPayment(
                billingKey.getBillingKey(),
                customerKey,
                orderId,
                orderName,
                amount
        );

        // Payment 저장
        Payment payment = Payment.builder()
                .subscription(subscription)
                .providerPaymentId(response.getPaymentKey())
                .amount(response.getTotalAmount())
                .orderId(orderId)
                .orderName(orderName)
                .status(response.getStatus().equals("DONE") ? PaymentStatus.APPROVED : PaymentStatus.FAILED)
                .paidAt(response.getApprovedAt() != null ? LocalDateTime.now() : null) // 실제로는 response 날짜 파싱 권장
                .build();
        paymentRepository.save(payment);

        return response;
    }

    /**
     * 특정 사용자의 결제 이력을 조회합니다.
     * @param customerKey 사용자 닉네임
     * @return Payment 엔티티 리스트
     */
    @Transactional(readOnly = true)
    public List<Payment> getHistory(String customerKey) {
        User user = userRepository.findByNickname(customerKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElseThrow(() -> new BusinessException("구독 정보를 찾을 수 없습니다.", ErrorCode.ENTITY_NOT_FOUND));

        return paymentRepository.findAll().stream()
                .filter(p -> p.getSubscription().getSubscriptionId().equals(subscription.getSubscriptionId()))
                .toList();
    }

        /**
     * 빌링키를 비활성화하고 연결된 구독을 취소합니다.
     * @param customerKey 사용자 닉네임
     */
    public void removeBillingKey(String customerKey) {
        User user = userRepository.findByNickname(customerKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findBySubscriber(user)
                .orElse(null);

        if (subscription != null) {
            subscription.cancel(); // Entity에 cancel 메서드 확인 필요, 없으면 아래처럼 직접 상태 변경
            // subscriptionRepository.save(...) 
            
            BillingKey billingKey = subscription.getBillingKey();
            if (billingKey != null) {
                // BillingKey 상태 변경 (REVOKED)
                BillingKey revokedKey = BillingKey.builder()
                        .billingKeyId(billingKey.getBillingKeyId())
                        .user(user)
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
                billingKeyRepository.save(revokedKey);
            }
        }
    }
}