package com.example.cinema.controller.subscription;

import com.example.cinema.config.auth.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.subscription.PaymentHistoryResponse;
import com.example.cinema.dto.subscription.SubscriptionCreateRequest;
import com.example.cinema.dto.subscription.SubscriptionResponse;
import com.example.cinema.dto.subscription.SubscriptionUpdateBillingRequest;
import com.example.cinema.service.subscription.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;


    /**
     * 플랫폼 구독 생성
     * POST /api/users/subscriptions
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        SubscriptionResponse response = subscriptionService.createSubscription(
                userDetails.getUser().getUserId(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("구독이 생성되었습니다.", response));
    }

    /**
     * 내 구독 정보 조회
     * GET /api/users/subscriptions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SubscriptionResponse response = subscriptionService.getMySubscription(
                userDetails.getUser().getUserId()
        );
        return ResponseEntity.ok(ApiResponse.success("구독 정보를 조회했습니다.", response));
    }

    /**
     * 구독 결제 내역 조회
     * GET /api/users/subscriptions/payments
     */
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentHistoryResponse>>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<PaymentHistoryResponse> response = subscriptionService.getPaymentHistory(
                userDetails.getUser().getUserId(),
                startDate,
                endDate,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("결제 내역을 조회했습니다.", response));
    }

    /**
     * 결제 수단(빌링키) 변경
     * PUT /api/users/subscriptions
     */
    @PutMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateBillingKey(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionUpdateBillingRequest request
    ) {
        SubscriptionResponse response = subscriptionService.updateBillingKey(
                userDetails.getUser().getUserId(),
                request
        );
        return ResponseEntity.ok(ApiResponse.success("결제 수단이 변경되었습니다.", response));
    }

    /**
     * 구독 해지
     * DELETE /api/users/subscriptions
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        subscriptionService.cancelSubscription(userDetails.getUser().getUserId());
        return ResponseEntity.ok(ApiResponse.success("구독이 해지되었습니다."));
    }
}

