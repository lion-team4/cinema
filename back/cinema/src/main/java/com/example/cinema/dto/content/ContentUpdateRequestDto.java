package com.example.cinema.dto.content;

import com.example.cinema.type.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContentUpdateRequestDto(
        @NotBlank
        @Size(min = 1, max = 50)
        String title,

        @NotBlank
        @Size(min = 1, max = 50)
        String description,

        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId,

        @NotNull
        ContentStatus status
) {}