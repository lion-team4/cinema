package com.example.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentRequestDto (
    @NotBlank
    @Size(min = 1, max = 50)
    String title,

    @NotBlank
    @Size(max =500)
    String description
){}
