package com.example.cinema.dto.content;

import com.example.cinema.type.ContentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContentPatchRequest {
        private String title;
        private String description;
        private ContentStatus status;

        private Long posterAssetId;
        private Long videoSourceAssetId;
        private Long videoHlsMasterAssetId;
}

