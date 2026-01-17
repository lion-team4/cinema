package com.example.cinema.repository.schedule.custom;

import com.example.cinema.dto.schedule.ScheduleSearchRequest;
import com.example.cinema.entity.QContent;
import com.example.cinema.entity.QScheduleDay;
import com.example.cinema.entity.QScheduleItem;
import com.example.cinema.entity.QUser;
import com.example.cinema.entity.ScheduleItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
public class ScheduleItemRepositoryImpl implements ScheduleItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ScheduleItem> search(ScheduleSearchRequest request) {
        QScheduleItem scheduleItem = QScheduleItem.scheduleItem;
        QContent content = QContent.content;
        QUser user = QUser.user;
        QScheduleDay scheduleDay = QScheduleDay.scheduleDay;

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 무조건 락된(확정된) 스케줄만 조회
        builder.and(scheduleDay.isLocked.isTrue());

        // 2. 닉네임 필터
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            builder.and(content.owner.nickname.eq(request.getNickname()));
        }

        // 2. 날짜 필터
        if (request.isDateFilter() && request.getStartDate() != null && request.getEndDate() != null) {
            LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
            LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);
            
            // 검색 기간 내에 시작하거나 끝나는 스케줄, 또는 기간을 포함하는 스케줄
            // startAt <= endDate && endAt >= startDate
            builder.and(scheduleItem.startAt.loe(endDateTime)
                    .and(scheduleItem.endAt.goe(startDateTime)));
        }

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;

        List<ScheduleItem> contentList = queryFactory
                .selectFrom(scheduleItem)
                .join(scheduleItem.content, content).fetchJoin()
                .join(content.owner, user).fetchJoin()
                .join(scheduleItem.scheduleDay, scheduleDay).fetchJoin()
                .where(builder)
                .orderBy(content.title.asc(), scheduleItem.startAt.asc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long count = queryFactory
                .select(scheduleItem.count())
                .from(scheduleItem)
                .join(scheduleItem.content, content)
                .join(scheduleItem.scheduleDay, scheduleDay)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(contentList, PageRequest.of(page, size), count != null ? count : 0L);
    }
}
