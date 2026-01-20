package com.example.cinema.dto.content;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @NotNull(message = "별점은 필수입니다.")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
    private Integer rating;

    /**
     * 리뷰 대상 콘텐츠 ID
     */
    @JsonAlias({"contentId", "content-id"})
    @NotNull(message = "콘텐츠 ID는 필수입니다.")
    private Long contentId;

    /**
     * 리뷰 내용 (선택 사항)
     */
    private String comment;
}