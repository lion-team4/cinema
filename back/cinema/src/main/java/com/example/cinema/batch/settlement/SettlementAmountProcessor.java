package com.example.cinema.batch.settlement;

import com.example.cinema.dto.settlement.ContentOwnerViewSummary;
import com.example.cinema.entity.Settlement;
import com.example.cinema.entity.User;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.SettlementStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 정산 금액 계산 Processor
 * 조회수당 100원 고정 단가로 계산
 * Skip 정책: 사용자 조회 실패 시 해당 항목을 건너뛰고 로그를 남김
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementAmountProcessor implements ItemProcessor<ContentOwnerViewSummary, Settlement> {
    
    private static final long SETTLEMENT_RATE_PER_VIEW = 100L; // 조회수당 100원
    
    private final UserRepository userRepository;
    
    private StepExecution stepExecution;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        
        // Job 파라미터에서 기간 가져오기
        // Spring Batch 5.x: JobParameters.getParameter()로 JobParameter를 가져온 후 getValue() 호출
        this.periodStart = LocalDate.parse(
            stepExecution.getJobParameters().getParameter("periodStart").getValue().toString()
        );
        this.periodEnd = LocalDate.parse(
            stepExecution.getJobParameters().getParameter("periodEnd").getValue().toString()
        );
    }
    
    @Override
    public Settlement process(ContentOwnerViewSummary item) throws Exception {
        // 정산 금액 계산: 조회수 × 100원 (고정 단가)
        Long settlementAmount = item.getTotalViews() * SETTLEMENT_RATE_PER_VIEW;
        
        // User 조회 - 실패 시 Skip 가능한 예외로 변환
        User creator = userRepository.findById(item.getOwnerId())
            .orElseThrow(() -> {
                log.warn("정산 대상 사용자를 찾을 수 없어 건너뜁니다: ownerId={}, email={}, nickname={}, totalViews={}", 
                    item.getOwnerId(), item.getOwnerEmail(), item.getOwnerNickname(), item.getTotalViews());
                // Skip 가능한 예외로 변환 (IllegalArgumentException은 기본적으로 Skip 가능)
                return new IllegalArgumentException("User not found: " + item.getOwnerId());
            });
        
        // Settlement 엔티티 생성
        return Settlement.builder()
            .creator(creator)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .totalViews(item.getTotalViews())
            .monthView(item.getTotalViews())
            .amount(settlementAmount)
            .status(SettlementStatus.PENDING)
            .build();
    }
}

