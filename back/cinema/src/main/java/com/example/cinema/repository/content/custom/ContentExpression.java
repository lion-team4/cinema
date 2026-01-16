package com.example.cinema.repository.content.custom;

import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.QContent;
import com.example.cinema.entity.QTag;
import com.example.cinema.entity.QTagMap;
import com.example.cinema.type.SortField;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;

import java.util.List;
import java.util.Objects;

public class ContentExpression {
    //태그리스트 전처리
    private static List<String> sanitizeTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private static BooleanExpression whitespaceIgnoreCase(StringExpression field, String keyword){
        if (keyword == null || keyword.isBlank()) return null;
        // JAVA 공백 제거
        String normalized = keyword.trim().replaceAll("\\s+", "");
        // 문자열 반환 강제(CONCAT) + 공백클래스([[:space:]])
        StringExpression normalizedField = Expressions.stringTemplate(
                "CONCAT('', REGEXP_REPLACE({0}, '[[:space:]]+', ''))",
                field
        );
        return normalizedField.containsIgnoreCase(normalized);
    }

    //tag 리스트중 하나라고 포함(OR)
    public static BooleanExpression oneOfTags(QContent content, List<String> tags){
        List<String> sanitizedTags = sanitizeTags(tags);
        if (sanitizedTags.isEmpty()) return null;

        return content.tagMaps.any().tag.name.in(sanitizedTags);
    }

    //게시물의 태그 중 하나라도 tags 목록에 포함되면 통과
    public static BooleanExpression hasAllTags(QContent content, List<String> tags){
        List<String> sanitizedTags = sanitizeTags(tags);
        if (sanitizedTags.isEmpty()) return null;

        QTagMap tagMap = QTagMap.tagMap;
        QTag tag = QTag.tag;

        var subQuery = JPAExpressions
                .select(tagMap.content.contentId)
                .from(tagMap)
                .join(tagMap.tag, tag)
                .where(tag.name.in(sanitizedTags))
                .groupBy(tagMap.content.contentId)
                .having(tag.name.countDistinct().eq((long) sanitizedTags.size()));

        return content.contentId.in(subQuery);
    }


    @QueryDelegate(Content.class)
    public static Predicate search(QContent content, ContentSearchRequest request) {
        String keyword = request.getKeyword();
        List<String> tags = sanitizeTags(request.getTags());

        BooleanBuilder builder = new BooleanBuilder();

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            builder.and(content.owner.nickname.eq(request.getNickname()));
        }

        if (!(keyword == null || keyword.isBlank())){
            BooleanExpression keywordExpression = request.isTitle() ?
                    whitespaceIgnoreCase(content.title, keyword) :
                    whitespaceIgnoreCase(content.owner.nickname, keyword);
            builder.and(keywordExpression);
        }


        if (request.isFilter()){
            BooleanExpression tagListExpression = request.isOr() ?
                    oneOfTags(content, tags) :
                    hasAllTags(content, tags);
            builder.and(tagListExpression);
        }
        return builder;
    }



    @QueryDelegate(Content.class)
    public static OrderSpecifier<?> sort(QContent content, ContentSearchRequest request){
        SortField sort = request.getSort();
        boolean asc = request.isAsc();

        return switch (sort){
            case VIEW -> asc ? content.totalView.asc() : content.totalView.desc();
            case CREATED -> asc ? content.createdAt.asc() : content.createdAt.desc();
            case UPDATED -> asc ? content.updatedAt.asc() : content.updatedAt.desc();
        };
    }
}
