package com.example.cinema.dto.payment.toss;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스 빌링키 발급 API 응답 DTO
 * <p>
 * 참고: https://docs.tosspayments.com/reference#%EB%B9%8C%EB%A7%81%ED%82%A4-%EB%B0%9C%EA%B8%89
 */
@Getter
@NoArgsConstructor
public class TossBillingResponse {
    private String mId; //상점
    private String customerKey; //고객키
    private String authenticatedAt; //인증시각
    private String method; //결제수단
    private String billingKey; //빌링키
    private Card card; //카드

    @Data
    public static class Card {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private String cardType;
        private String ownerType;
    }
}
