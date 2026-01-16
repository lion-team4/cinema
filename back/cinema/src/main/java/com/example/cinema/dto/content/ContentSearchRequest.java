package com.example.cinema.dto.content;

import com.example.cinema.type.SortField;
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
@Setter
public class ContentSearchRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String keyword = "";
    private boolean title = true;
    private boolean filter = false;
    private List<String> tags;
    private boolean or = true;
    private SortField sort = SortField.CREATED;
    private boolean asc = false;
    private String nickname;
}
