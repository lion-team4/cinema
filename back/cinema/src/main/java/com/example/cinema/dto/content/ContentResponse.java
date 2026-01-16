package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentResponse {
    private Long content_id;
    private String title;
    private String description;
    private String status;

    private Long posterAssetId;
    private Long videoSourceAssetId;
    private Long videoHlsMasterAssetId;

    public static ContentResponse from(Content c) {
        return ContentResponse.builder()
                .content_id(c.getContentId())
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus().name())
                .posterAssetId(c.getPoster() == null ? null : c.getPoster().getAssetId())
                .videoSourceAssetId(c.getVideoSource() == null ? null : c.getVideoSource().getAssetId())
                .videoHlsMasterAssetId(c.getVideoHlsMaster() == null ? null : c.getVideoHlsMaster().getAssetId())
                .build();
    }
}
