package com.example.cinema.infrastructure.payment.toss;

import com.example.cinema.dto.payment.toss.TossBillingResponse;
import com.example.cinema.dto.payment.toss.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 토스 페이먼츠 API 호출 클라이언트
 */
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final RestClient tossRestClient;

    /**
     * 빌링키 발급 요청
     * @param authKey 프론트엔드 인증 키
     * @param customerKey 고객 식별 키
     */
    public TossBillingResponse issueBillingKey(String authKey, String customerKey) {
        return tossRestClient.post()
                .uri("/billing/authorizations/issue")
                .body(Map.of(
                        "authKey", authKey,
                        "customerKey", customerKey
                ))
                .retrieve()
                .body(TossBillingResponse.class);
    }

    /**
     * 자동 결제(빌링) 승인 요청
     */
    public TossPaymentResponse requestPayment(String billingKey, String customerKey, String orderId, String orderName, Long amount) {
        return tossRestClient.post()
                .uri("/billing/" + billingKey)
                .body(Map.of(
                        "customerKey", customerKey,
                        "orderId", orderId,
                        "orderName", orderName,
                        "amount", amount
                ))
                .retrieve()
                .body(TossPaymentResponse.class);
    }
}
