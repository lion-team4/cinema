package com.example.cinema.batch.settlement;

import com.example.cinema.entity.Settlement;
import com.example.cinema.entity.User;
import com.example.cinema.repository.settlement.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Settlement 저장 Writer (멱등성 보장)
 * 동일 기간/크리에이터로 이미 정산 레코드가 있으면 삭제 후 재생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementWriter implements ItemWriter<Settlement> {
    
    private final SettlementRepository settlementRepository;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
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
    @Transactional
    public void write(Chunk<? extends Settlement> chunk) throws Exception {
        for (Settlement settlement : chunk) {
            User creator = settlement.getCreator();
            
            // 멱등성 보장: 동일 기간/크리에이터로 이미 정산 레코드가 있는지 확인
            settlementRepository
                .findByCreatorAndPeriodStartAndPeriodEnd(
                    creator,
                    periodStart,
                    periodEnd
                )
                .ifPresent(existing -> {
                    log.warn("이미 존재하는 정산 레코드 삭제 후 재생성: creatorId={}, period={}~{}", 
                        creator.getUserId(), periodStart, periodEnd);
                    settlementRepository.delete(existing);
                });
            
            // 새 정산 레코드 저장
            settlementRepository.save(settlement);
        }
    }
}

