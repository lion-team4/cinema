package com.example.cinema.batch.settlement;

import com.example.cinema.dto.settlement.ContentOwnerViewSummary;
import com.example.cinema.entity.QContent;
import com.example.cinema.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Content Owner별 조회수 집계 Reader (페이징 기반)
 * monthView >= 10인 Content만 정산 대상으로 조회
 */
@Component
@RequiredArgsConstructor
public class ContentOwnerViewReader extends AbstractPagingItemReader<ContentOwnerViewSummary> {
    
    private final JPAQueryFactory queryFactory;
    
    @PostConstruct
    public void init() {
        setPageSize(50); // Chunk Size와 동일하게 설정
    }
    
    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }
        
        QContent content = QContent.content;
        QUser user = QUser.user;
        
        // 페이징 기반 조회 (QueryDSL 사용)
        // monthView >= 10인 Content만 정산 대상
        List<ContentOwnerViewSummary> pageResults = queryFactory
            .select(Projections.constructor(
                ContentOwnerViewSummary.class,
                user.userId,
                user.email,
                user.nickname,
                content.monthView.sum()
            ))
            .from(content)
            .join(content.owner, user)
            .where(content.monthView.goe(10)) // 조회수 10 이상만 정산 대상
            .groupBy(user.userId, user.email, user.nickname)
            .orderBy(user.userId.asc())
            .offset(getPage() * getPageSize())
            .limit(getPageSize())
            .fetch();
        
        results.addAll(pageResults);
    }
}

