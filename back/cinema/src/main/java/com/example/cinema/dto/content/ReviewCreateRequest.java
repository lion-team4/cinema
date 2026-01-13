package com.example.cinema.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 생성/수정 요청 DTO
 * <p>
 * 용도:
 * - 리뷰 작성 (POST /contents/reviews)
 * - 리뷰 수정 (PUT /contents/reviews/{id})
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCreateRequest {

    /**
     * 별점 (1~5 정수)
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    /**
     * 리뷰 대상 콘텐츠 ID
     */
    @JsonProperty("content-id")
    @NotNull(message = "Content ID is required")
    private Long contentId;

    /**
     * 리뷰 내용 (선택 사항)
     */
    private String comment;
}