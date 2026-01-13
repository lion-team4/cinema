package com.example.cinema.infrastructure.payment.toss.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토스 결제 승인 API 응답 DTO (정기 결제용)
 * <p>
 * 참고: https://docs.tosspayments.com/reference#%EB%B9%8C%EB%A7%81-%EA%B2%B0%EC%A0%9C-%EC%9A%94%EC%B2%AD
 */
@Data
@NoArgsConstructor
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private String type;
    private Card card;
    private Receipt receipt;
    private Checkout checkout;
    private String currency;
    private Long totalAmount;
    private Long balanceAmount;
    private Long suppliedAmount;
    private Long vat;
    private Long taxFreeAmount;
    private String method;
    private String version;

    @Data
    public static class Card {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private Boolean isInterestFree;
        private String interestPayer;
    }

    @Data
    public static class Receipt {
        private String url;
    }

    @Data
    public static class Checkout {
        private String url;
    }

}
