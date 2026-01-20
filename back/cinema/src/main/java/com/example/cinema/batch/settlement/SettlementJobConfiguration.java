package com.example.cinema.batch.settlement;

import com.example.cinema.batch.settlement.ContentOwnerViewReader;
import com.example.cinema.batch.settlement.MonthViewResetProcessor;
import com.example.cinema.batch.settlement.SettlementAmountProcessor;
import com.example.cinema.batch.settlement.SettlementWriter;
import com.example.cinema.dto.settlement.ContentOwnerViewSummary;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.Settlement;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 정산 Batch Job 설정
 * Skip 정책: 사용자 조회 실패 등 특정 예외 발생 시 해당 항목을 건너뛰고 계속 진행
 */
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfiguration {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final ContentOwnerViewReader contentOwnerViewReader;
    private final SettlementAmountProcessor settlementAmountProcessor;
    private final SettlementWriter settlementWriter;
    private final MonthViewResetProcessor monthViewResetProcessor;
    
    @Bean
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
            .start(processSettlementStep())
//            .next(resetMonthViewStep())
            .build();
    }
    
    @Bean
    public Step processSettlementStep() {
        return new StepBuilder("processSettlementStep", jobRepository)
            .<ContentOwnerViewSummary, Settlement>chunk(50, transactionManager)
            .reader(contentOwnerViewReader)
            .processor(settlementAmountProcessor)
            .writer(settlementWriter)
            .faultTolerant()
            .skip(IllegalArgumentException.class) // 사용자 조회 실패 등 Skip 가능한 예외
            .skipLimit(100) // 최대 100개까지 Skip 허용 (전체 실패 방지)
            .build();
    }
    
    @Bean
    public Step resetMonthViewStep() {
        return new StepBuilder("resetMonthViewStep", jobRepository)
            .<Content, Content>chunk(100, transactionManager)
            .reader(contentReader())
            .processor(monthViewResetProcessor)
            .writer(contentWriter())
            .build();
    }
    
    @Bean
    public JpaPagingItemReader<Content> contentReader() {
        return new JpaPagingItemReaderBuilder<Content>()
            .name("contentReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(100)
            .queryString("SELECT c FROM Content c")
            .build();
    }
    
    @Bean
    public JpaItemWriter<Content> contentWriter() {
        JpaItemWriter<Content> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}

