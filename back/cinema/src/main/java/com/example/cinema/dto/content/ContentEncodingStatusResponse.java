package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;

public record ContentEncodingStatusResponse(
        Long contentId,
        String encodingStatus,
        String encodingError
) {
    public static ContentEncodingStatusResponse from(Content content) {
        String status = content.getEncodingStatus() == null ? null : content.getEncodingStatus().name();
        return new ContentEncodingStatusResponse(
                content.getContentId(),
                status,
                content.getEncodingError()
        );
    }
}
