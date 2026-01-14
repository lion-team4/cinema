package com.example.cinema.asset.transcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadCompleteRequest(
        @NotNull Long movieId,
        @NotBlank String sourceKey
) {}

