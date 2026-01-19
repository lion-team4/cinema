package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;
import com.example.cinema.type.ContentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
     * 포스터 이미지 URL (목록 썸네일용)
     */
    private final String posterImage;
    /**
     * 콘텐츠 상태 (PUBLISHED, DRAFT 등)
     */
    private final ContentStatus status;
    private final String ownerNickname;
    private final Long totalView;
    private final Long durationMs;


    public static ContentSearchResponse from(Content content) {
        return from(content, content.getPoster() != null ? content.getPoster().getObjectKey() : null);
    }

    public static ContentSearchResponse from(Content content, String posterImageUrl) {
        Long durationMs = null;
        if (content.getVideoHlsMaster() != null) {
            durationMs = content.getVideoHlsMaster().getDurationMs();
        } else if (content.getVideoSource() != null) {
            durationMs = content.getVideoSource().getDurationMs();
        }

        return new ContentSearchResponse(
                content.getContentId(),
                content.getTitle(),
                content.getDescription(),
                posterImageUrl,
                content.getStatus(),
                content.getOwner().getNickname(),
                content.getTotalView(),
                durationMs
        );
    }
}