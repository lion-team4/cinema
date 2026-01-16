package com.example.cinema.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentRequest {
    @NotBlank(message = "제목은 필수 입력값입니다.")
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "설명은 필수 입력값입니다.")
    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;
}
