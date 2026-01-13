package com.example.cinema.dto.content;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 콘텐츠 검색 필터 요청 DTO
 * <p>
 * 용도:
 * - 콘텐츠 검색 및 목록 조회 시 다양한 필터링 조건 전달
 * - GET /contents/search
 */
@Getter
@Setter // 쿼리 파라미터 바인딩을 위해 Setter 필요
public class ContentSearchRequest {
    /**
     * 검색 키워드 (제목 혹은 감독/크리에이터)
     */
    private String keyword;

    /**
     * 검색 기준 (title, owner 등)
     */
    private String searchType;

    /**
     * 태그 목록 (다중 선택 가능)
     */
    private List<String> tags;

    /**
     * 태그 검색 모드 (OR / AND)
     */
    private String tagMode = "AND";

    /**
     * 정렬 기준 (view, createdAt 등)
     */
    private String sort = "createdAt";

    /**
     * 오름차순 여부
     */
    private Boolean asc = false;
}
