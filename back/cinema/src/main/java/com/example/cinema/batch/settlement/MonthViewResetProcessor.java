package com.example.cinema.batch.settlement;

import com.example.cinema.entity.Content;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * monthView 초기화 Processor
 * 정산 완료 후 다음 달 조회수 집계를 위해 monthView를 0으로 설정
 */
@Component
public class MonthViewResetProcessor implements ItemProcessor<Content, Content> {
    
    @Override
    public Content process(Content item) throws Exception {
        // monthView를 0으로 초기화
        item.resetMonthView();
        return item;
    }
}

