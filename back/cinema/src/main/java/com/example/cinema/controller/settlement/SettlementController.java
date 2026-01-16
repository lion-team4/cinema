package com.example.cinema.controller.settlement;

import com.example.cinema.config.common.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.settlement.SettlementExecutionResponse;
import com.example.cinema.dto.settlement.SettlementListResponse;
import com.example.cinema.dto.settlement.SettlementStatsResponse;
import com.example.cinema.service.settlement.SettlementService;
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

import java.time.LocalDate;

/**
 * 정산 관련 API Controller
 */
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlement", description = "정산 관련 API")
public class SettlementController {
    
    private final SettlementService settlementService;
    
    @Operation(summary = "정산 내역 조회", description = "로그인한 사용자(크리에이터)의 정산 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SettlementListResponse>>> getSettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Long userId = userDetails.getUser().getUserId();
        PageResponse<SettlementListResponse> response = settlementService.getSettlements(
            userId, startDate, endDate, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("정산 내역 조회 성공", response));
    }
    
    @Operation(summary = "정산 통계 조회", description = "로그인한 사용자의 정산 통계를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SettlementStatsResponse>> getSettlementStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getUserId();
        SettlementStatsResponse response = settlementService.getSettlementStats(userId);
        return ResponseEntity.ok(ApiResponse.success("정산 통계 조회 성공", response));
    }
    
    @Operation(summary = "정산 수동 실행", description = "관리자가 정산 Job을 수동으로 실행합니다.")
    @PostMapping("/admin/execute")
    public ResponseEntity<ApiResponse<SettlementExecutionResponse>> executeSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SettlementExecutionRequest request) {
        
        SettlementExecutionResponse response = settlementService.executeSettlement(
            request.getPeriodStart(),
            request.getPeriodEnd()
        );
        return ResponseEntity.ok(ApiResponse.success("정산 Job 실행 완료", response));
    }
    
    /**
     * 정산 실행 요청 DTO
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SettlementExecutionRequest {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate periodStart;
        
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate periodEnd;
    }
}

