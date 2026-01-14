package com.example.cinema.dto.content;

import com.example.cinema.type.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentUpdateRequest {
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    @NotBlank
    @Size(min = 1, max = 50)
    private String description;

    private Long posterAssetId;
    private Long videoSourceAssetId;
    private Long videoHlsMasterAssetId;

    @NotNull
    private ContentStatus status;
}
