package com.example.cinema.repository.content.custom;

import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.QContent;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Content> searchContent(ContentSearchRequest request) {

        QContent content = QContent.content;

        int page = request.getPage();
        int size = request.getSize();

        var query = jpaQueryFactory
                .selectFrom(content)
                .distinct()
                .where(content.search(request));

        OrderSpecifier<?> order = content
                .sort(request);
        query.orderBy(order);

        List<Content> result = query
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long total = jpaQueryFactory
                .select(content.contentId.countDistinct())
                .from(content)
                .where(content.search(request))
                .fetchOne();

        total = (total == null) ? 0 : total;

        return new PageImpl<>(result, PageRequest.of(page, size), total);
    }
}
