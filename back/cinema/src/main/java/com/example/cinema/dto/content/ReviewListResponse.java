package com.example.cinema.dto.content;

import com.example.cinema.entity.Review;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 리뷰 목록 응답 DTO
 * <p>
 * 용도:
 * - 특정 콘텐츠의 리뷰 목록 조회 (GET /contents/reviews/search/{contentId})
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewListResponse {
    private final Long reviewId;
    private final Integer rating;
    private final String comment;
    /**
     * 작성자 닉네임 (익명성 보장 수준에 따라 조정 가능)
     */
    private final String writerNickname;
    private final LocalDateTime createdAt;

    public static ReviewListResponse from(Review review) {
        return new ReviewListResponse(
                review.getReviewId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getNickname(),
                review.getCreatedAt()
        );
    }
}