package com.example.cinema.controller.subscription;

import com.example.cinema.config.auth.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.subscription.*;
import com.example.cinema.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/subscriptions")
@Tag(name = "Subscription", description = "구독 관련 API")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<FirstSubscriptionResponse>> createSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionCreateRequest request) {
        Long userId = userDetails.getUser().getUserId();
        FirstSubscriptionResponse response = subscriptionService.createSubscription(userId, request);
        return ResponseEntity.ok(ApiResponse.success("구독 생성이 완료되었습니다.", response));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getUserId();
        SubscriptionResponse response = subscriptionService.getMySubscription(userId);
        return ResponseEntity.ok(ApiResponse.success("구독 정보 조회 성공", response));
    }


    @GetMapping("/payment-history")
    public ResponseEntity<ApiResponse<PageResponse<PaymentHistoryResponse>>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = userDetails.getUser().getUserId();
        PageResponse<PaymentHistoryResponse> response = subscriptionService.getPaymentHistory(
                userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("결제 내역 조회 성공", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateBillingKey(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionUpdateBillingRequest request) {
        Long userId = userDetails.getUser().getUserId();
        SubscriptionResponse response = subscriptionService.updateBillingKey(userId, request);
        return ResponseEntity.ok(ApiResponse.success("결제 수단 변경이 완료되었습니다.", response));
    }


    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getUserId();
        subscriptionService.cancelSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success("구독 해지가 완료되었습니다."));
    }
}

