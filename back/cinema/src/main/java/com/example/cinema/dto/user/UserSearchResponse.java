package com.example.cinema.dto.user;

import com.example.cinema.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 유저 검색 목록 응답 DTO
 * <p>
 * 용도:
 * - 유저 검색 (GET /users/search/{keyword})
 * - 리스트 형태의 경량화된 유저 정보 제공
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSearchResponse {
    private final Long userId;
    private final String nickname;
    /**
     * 목록 썸네일용 S3 Object Key
     */
    private final String profileImage;

    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
                user.getUserId(),
                user.getNickname(),
                user.getProfileImage() != null ? user.getProfileImage().getObjectKey() : null
        );
    }
}