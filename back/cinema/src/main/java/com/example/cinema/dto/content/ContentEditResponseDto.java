package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;
import com.example.cinema.type.ContentStatus;

public record ContentEditResponseDto(
        String title,
        String description,

        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId,

        ContentStatus status
) {
    public static ContentEditResponseDto from(Content c) {
        return new ContentEditResponseDto(
                c.getTitle(),
                c.getDescription(),
                c.getPoster() == null ? null : c.getPoster().getAssetId(),
                c.getVideoSource() == null ? null : c.getVideoSource().getAssetId(),
                c.getVideoHlsMaster() == null ? null : c.getVideoHlsMaster().getAssetId(),
                c.getStatus()
        );
    }
}