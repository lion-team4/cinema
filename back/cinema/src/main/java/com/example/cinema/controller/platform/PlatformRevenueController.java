package com.example.cinema.controller.platform;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.platform.MonthlyPlatformRevenueResponse;
import com.example.cinema.dto.platform.PlatformRevenueResponse;
import com.example.cinema.dto.platform.PlatformRevenueStatsResponse;
import com.example.cinema.service.platform.PlatformRevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 플랫폼 수입 관련 API Controller
 * 인증 없이 누구나 조회 가능
 */
@RestController
@RequestMapping("/admin/platform-revenue")
@RequiredArgsConstructor
@Tag(name = "Platform Revenue", description = "플랫폼 수입 관련 API")
public class PlatformRevenueController {
    
    private final PlatformRevenueService platformRevenueService;
    
    @Operation(summary = "플랫폼 수입 조회", description = "기간별 플랫폼 수입을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PlatformRevenueResponse>> getPlatformRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 기본값: 전월
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.minusMonths(1).withDayOfMonth(1);
            endDate = now.minusMonths(1).withDayOfMonth(
                now.minusMonths(1).lengthOfMonth()
            );
        }
        
        PlatformRevenueResponse response = platformRevenueService.getPlatformRevenue(
            startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("플랫폼 수입 조회 성공", response));
    }
    
    @Operation(summary = "플랫폼 수입 통계 조회", description = "전체 기간 플랫폼 수입 통계를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PlatformRevenueStatsResponse>> getPlatformRevenueStats() {
        
        PlatformRevenueStatsResponse response = platformRevenueService.getPlatformRevenueStats();
        return ResponseEntity.ok(ApiResponse.success("플랫폼 수입 통계 조회 성공", response));
    }
    
    @Operation(summary = "월별 플랫폼 수입 내역 조회", description = "월별 플랫폼 수입 내역을 조회합니다.")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<PageResponse<MonthlyPlatformRevenueResponse>>> getMonthlyPlatformRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        PageResponse<MonthlyPlatformRevenueResponse> response = platformRevenueService.getMonthlyPlatformRevenue(
            startDate, endDate, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("월별 플랫폼 수입 조회 성공", response));
    }
}

