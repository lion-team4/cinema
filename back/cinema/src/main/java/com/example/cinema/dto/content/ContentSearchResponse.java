package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;
import com.example.cinema.type.ContentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 콘텐츠 검색 및 목록 조회 응답 DTO
 * <p>
 * 용도:
 * - 콘텐츠 검색 (GET /contents/search)
 * - 유저별 콘텐츠 목록 (GET /users/{nick}/contents)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentSearchResponse {
    private final Long contentId;
    private final String title;
    private final String description;
    /**
     * 포스터 이미지 S3 Key (목록 썸네일용)
     */
    private final String posterImage;
    /**
     * 콘텐츠 상태 (PUBLISHED, DRAFT 등)
     */
    private final ContentStatus status;
    private final String ownerNickname;
    private final Long totalView;


    public static ContentSearchResponse from(Content content) {
        return new ContentSearchResponse(
                content.getContentId(),
                content.getTitle(),
                content.getDescription(),
                content.getPoster() != null ? content.getPoster().getObjectKey() : null,
                content.getStatus(),
                content.getOwner().getNickname(),
                content.getTotalView()
        );
    }
}