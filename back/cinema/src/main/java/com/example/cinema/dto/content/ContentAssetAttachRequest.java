package com.example.cinema.dto.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContentAssetAttachRequest {
    private Long posterAssetId;
    private Long videoSourceAssetId;
    private Long videoHlsMasterAssetId;
}
