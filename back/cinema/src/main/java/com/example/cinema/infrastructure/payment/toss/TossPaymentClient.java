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
        try {
            return tossRestClient.post()
                    .uri("/billing/authorizations/issue")
                    .body(Map.of(
                            "authKey", authKey,
                            "customerKey", customerKey
                    ))
                    .retrieve()
                    .body(TossBillingResponse.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("토스페이먼츠 API 인증 실패: secret-key가 올바르지 않거나 만료되었습니다. application.yaml의 toss.secret-key를 확인해주세요.", e);
            } else if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("토스페이먼츠 API 호출 실패: authKey가 유효하지 않거나 만료되었습니다. 카드 등록을 다시 시도해주세요. (404 Not Found)", e);
            }
            throw new RuntimeException("토스페이먼츠 API 호출 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("토스페이먼츠 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 자동 결제(빌링) 승인 요청
     */
    public TossPaymentResponse requestPayment(String billingKey, String customerKey, String orderId, String orderName, Long amount) {
        try {
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
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("토스페이먼츠 API 인증 실패: secret-key가 올바르지 않거나 만료되었습니다. application.yaml의 toss.secret-key를 확인해주세요.", e);
            }
            throw new RuntimeException("토스페이먼츠 API 호출 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("토스페이먼츠 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
