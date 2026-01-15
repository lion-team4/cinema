package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;
import com.example.cinema.type.ContentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentEditResponse {
    private String title;
    private String description;

    private Long posterAssetId;
    private Long videoSourceAssetId;
    private Long videoHlsMasterAssetId;

    private ContentStatus status;

    public static ContentEditResponse from(Content c) {
        return ContentEditResponse.builder()
                .title(c.getTitle())
                .description(c.getDescription())
                .posterAssetId(c.getPoster() == null ? null : c.getPoster().getAssetId())
                .videoSourceAssetId(c.getVideoSource() == null ? null : c.getVideoSource().getAssetId())
                .videoHlsMasterAssetId(c.getVideoHlsMaster() == null ? null : c.getVideoHlsMaster().getAssetId())
                .status(c.getStatus())
                .build();
    }
}