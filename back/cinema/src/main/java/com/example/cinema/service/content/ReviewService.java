package com.example.cinema.service.content;

import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.content.ReviewCreateRequest;
import com.example.cinema.dto.content.ReviewListResponse;
import com.example.cinema.dto.content.ReviewUpdateRequest;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.Review;
import com.example.cinema.entity.User;
import com.example.cinema.entity.WatchHistory;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.EntityNotFoundException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.content.ReviewRepository;
import com.example.cinema.repository.watchHistory.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final ContentRepository contentRepository;

    /**
     * 리뷰 생성
     * - 조건: 해당 콘텐츠에 대해 viewCounted=true 인 시청 기록이 있어야 함.
     * - 조건: 해당 시청 기록으로 아직 리뷰를 작성하지 않았어야 함.
     * - 정책: 가장 오래된(먼저 시청한) 사용 가능한 시청 기록에 매핑.
     */
    @Transactional
    public void createReview(User user, ReviewCreateRequest request) {
        Content content = contentRepository.findById(request.getContentId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CONTENT_NOT_FOUND.getMessage(), ErrorCode.CONTENT_NOT_FOUND));

        // 가용한 시청 기록 조회 (1개만 가져옴)
        List<WatchHistory> histories = watchHistoryRepository.findUsableWatchHistories(
                user, content, PageRequest.of(0, 1)
        );

        if (histories.isEmpty()) {
            throw new BusinessException("리뷰를 작성할 수 있는 시청 기록이 없습니다. (시청 완료 필요)", ErrorCode.REVIEW_NOT_ALLOWED);
        }

        WatchHistory watchHistory = histories.get(0);

        Review review = Review.builder()
                .user(user)
                .content(content)
                .watchHistory(watchHistory)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public void updateReview(User user, Long reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND.getMessage(), ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("리뷰 작성자만 수정할 수 있습니다.", ErrorCode.ACCESS_DENIED);
        }

        review.update(request.getRating(), request.getComment());
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND.getMessage(), ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException("리뷰 작성자만 삭제할 수 있습니다.", ErrorCode.ACCESS_DENIED);
        }

        reviewRepository.delete(review);
    }

    /**
     * 리뷰 목록 조회
     */
    public PageResponse<ReviewListResponse> getReviews(Long contentId, Pageable pageable) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CONTENT_NOT_FOUND.getMessage(), ErrorCode.CONTENT_NOT_FOUND));

        // 최신순 정렬 강제 적용 (또는 클라이언트 요청 따름) -> 여기서는 요청 따르되 기본값 설정 권장
        Page<Review> reviewPage = reviewRepository.findByContent(content, pageable);

        Page<ReviewListResponse> responsePage = reviewPage.map(ReviewListResponse::from);

        return PageResponse.from(responsePage);
    }
}
