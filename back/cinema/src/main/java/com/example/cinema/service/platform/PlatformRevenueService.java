package com.example.cinema.service.platform;

import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.platform.MonthlyPlatformRevenueResponse;
import com.example.cinema.dto.platform.PlatformRevenueResponse;
import com.example.cinema.dto.platform.PlatformRevenueStatsResponse;
import com.example.cinema.repository.payment.PaymentRepository;
import com.example.cinema.repository.settlement.SettlementRepository;
import com.example.cinema.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 플랫폼 수입 서비스 (관리자용)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformRevenueService {
    
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    
    /**
     * 플랫폼 수입 조회
     */
    public PlatformRevenueResponse getPlatformRevenue(
            LocalDate startDate,
            LocalDate endDate) {
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 총 구독 결제 금액
        Long totalPaymentAmount = paymentRepository.sumAmountByStatusAndPaidAtBetween(
            PaymentStatus.APPROVED,
            startDateTime,
            endDateTime
        );
        if (totalPaymentAmount == null) {
            totalPaymentAmount = 0L;
        }
        
        // 총 정산 금액
        Long totalSettlementAmount = settlementRepository.sumAmountByPeriod(startDate, endDate);
        if (totalSettlementAmount == null) {
            totalSettlementAmount = 0L;
        }
        
        // 플랫폼 수입
        Long platformRevenue = totalPaymentAmount - totalSettlementAmount;
        
        return new PlatformRevenueResponse(
            startDate,
            endDate,
            totalPaymentAmount,
            totalSettlementAmount,
            platformRevenue
        );
    }
    
    /**
     * 플랫폼 수입 통계 조회
     */
    public PlatformRevenueStatsResponse getPlatformRevenueStats() {
        // 전체 구독 결제 금액
        Long totalPaymentAmount = paymentRepository.sumTotalAmountByStatus(PaymentStatus.APPROVED);
        if (totalPaymentAmount == null) {
            totalPaymentAmount = 0L;
        }
        
        // 전체 정산 금액
        Long totalSettlementAmount = settlementRepository.sumTotalAmount();
        if (totalSettlementAmount == null) {
            totalSettlementAmount = 0L;
        }
        
        // 전체 플랫폼 수입
        Long totalPlatformRevenue = totalPaymentAmount - totalSettlementAmount;
        
        // 정산 건수로 월평균 계산
        long totalSettlements = settlementRepository.count();
        Long averageMonthlyRevenue = totalSettlements > 0 
            ? totalPlatformRevenue / totalSettlements 
            : 0L;
        
        // 전월 정산 조회
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(
            now.minusMonths(1).lengthOfMonth()
        );
        
        Long lastMonthPayment = paymentRepository.sumAmountByStatusAndPaidAtBetween(
            PaymentStatus.APPROVED,
            lastMonthStart.atStartOfDay(),
            lastMonthEnd.atTime(23, 59, 59)
        );
        if (lastMonthPayment == null) {
            lastMonthPayment = 0L;
        }
        
        Long lastMonthSettlement = settlementRepository.sumAmountByPeriod(lastMonthStart, lastMonthEnd);
        if (lastMonthSettlement == null) {
            lastMonthSettlement = 0L;
        }
        
        Long lastMonthRevenue = lastMonthPayment - lastMonthSettlement;
        
        return new PlatformRevenueStatsResponse(
            totalPlatformRevenue,
            totalPaymentAmount,
            totalSettlementAmount,
            averageMonthlyRevenue,
            lastMonthRevenue,
            lastMonthStart
        );
    }
    
    /**
     * 월별 플랫폼 수입 내역 조회
     * DB에서 직접 월별로 집계하여 성능 최적화
     */
    public PageResponse<MonthlyPlatformRevenueResponse> getMonthlyPlatformRevenue(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        
        // DB에서 월별 정산 금액 집계 (GROUP BY 연-월)
        List<Object[]> monthlySettlements = settlementRepository.findMonthlySettlementSummary(
            startDate, endDate
        );
        
        // 각 월별로 플랫폼 수입 계산
        List<MonthlyPlatformRevenueResponse> monthlyRevenues = monthlySettlements.stream()
            .map(row -> {
                // row[0]: year (Integer)
                // row[1]: month (Integer)
                // row[2]: periodStart (LocalDate)
                // row[3]: periodEnd (LocalDate)
                // row[4]: totalSettlementAmount (Long)
                
                Integer year = (Integer) row[0];
                Integer month = (Integer) row[1];
                LocalDate periodStart = convertToLocalDate(row[2]);
                LocalDate periodEnd = convertToLocalDate(row[3]);
                Long totalSettlementAmount = ((Number) row[4]).longValue();
                
                // 해당 월의 결제 금액 조회
                LocalDateTime startDateTime = periodStart.atStartOfDay();
                LocalDateTime endDateTime = periodEnd.atTime(23, 59, 59);
                
                Long totalPaymentAmount = paymentRepository.sumAmountByStatusAndPaidAtBetween(
                    PaymentStatus.APPROVED,
                    startDateTime,
                    endDateTime
                );
                if (totalPaymentAmount == null) {
                    totalPaymentAmount = 0L;
                }
                
                // 플랫폼 수입 계산
                Long platformRevenue = totalPaymentAmount - totalSettlementAmount;
                
                return new MonthlyPlatformRevenueResponse(
                    periodStart,
                    periodEnd,
                    totalPaymentAmount,
                    totalSettlementAmount,
                    platformRevenue
                );
            })
            .collect(Collectors.toList());
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), monthlyRevenues.size());
        List<MonthlyPlatformRevenueResponse> pagedContent = 
            start < monthlyRevenues.size() 
                ? monthlyRevenues.subList(start, end) 
                : List.of();
        
        // Page 객체 생성
        Page<MonthlyPlatformRevenueResponse> page = new org.springframework.data.domain.PageImpl<>(
            pagedContent,
            pageable,
            monthlyRevenues.size()
        );
        
        return PageResponse.from(page);
    }

    // 안전한 변환을 위한 헬퍼 메서드 (클래스 내부에 추가)
    private LocalDate convertToLocalDate(Object dateObject) {
        if (dateObject instanceof java.sql.Date) {
            return ((java.sql.Date) dateObject).toLocalDate();
        }
        return (LocalDate) dateObject;
    }
}

