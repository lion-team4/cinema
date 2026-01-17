package com.example.cinema.infrastructure.payment.toss;

import com.example.cinema.infrastructure.payment.toss.dto.TossBillingResponse;
import com.example.cinema.infrastructure.payment.toss.dto.TossPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@Profile("test")
@Primary
public class MockTossPaymentClient extends TossPaymentClient {

    public MockTossPaymentClient() {
        super(null);
    }

    @Override
    public TossBillingResponse issueBillingKey(String authKey, String customerKey) {
        log.info("[Mock] issueBillingKey called with authKey={}, customerKey={}", authKey, customerKey);
        TossBillingResponse response = new TossBillingResponse();
        response.setMId("tvivarepublica");
        response.setCustomerKey(customerKey);
        response.setAuthenticatedAt(LocalDateTime.now().toString());
        response.setMethod("카드");
        response.setBillingKey("TEST_BILLING_KEY_" + System.currentTimeMillis());
        
        TossBillingResponse.Card card = new TossBillingResponse.Card();
        card.setIssuerCode("11");
        card.setAcquirerCode("11");
        card.setNumber("123456******7890");
        card.setCardType("신용");
        card.setOwnerType("개인");
        response.setCard(card);
        
        return response;
    }

    @Override
    public TossPaymentResponse requestPayment(String billingKey, String customerKey, String orderId, String orderName, Long amount) {
        log.info("[Mock] requestPayment called with billingKey={}, orderId={}, amount={}", billingKey, orderId, amount);
        TossPaymentResponse response = new TossPaymentResponse();
        response.setPaymentKey("TEST_PAYMENT_KEY_" + System.currentTimeMillis());
        response.setOrderId(orderId);
        response.setOrderName(orderName);
        response.setStatus("DONE"); 
        
        // OffsetDateTime format is expected by Payment.create (e.g. 2022-01-01T00:00:00+09:00)
        String now = OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString();
        response.setRequestedAt(now);
        response.setApprovedAt(now);
        
        response.setType("NORMAL");
        response.setTotalAmount(amount);
        response.setBalanceAmount(amount); // Important for Payment.create
        response.setMethod("카드");
        
        return response;
    }
}
