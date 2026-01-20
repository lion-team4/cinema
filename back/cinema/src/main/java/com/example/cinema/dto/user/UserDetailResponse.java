package com.example.cinema.dto.user;

import com.example.cinema.entity.User;
import com.example.cinema.util.CdnUrlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 상세 프로필 응답 DTO
 * <p>
 * 용도:
 * - 내 정보 조회 (GET /users/me)
 * - 타 유저 프로필 조회 (GET /users/search/{nick}/info)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDetailResponse {
    private final Long userId;
    private final String email;
    private final String nickname;
    /**
     * S3 Object Key (Pre-signed URL 생성 등에 사용)
     */
    private final String profileImage;
    /**
     * 크리에이터(판매자) 여부4
     */
    private final Boolean seller;

    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage() != null ? user.getProfileImage().getObjectKey() : null,
                user.getSeller()
        );
    }

    public static UserDetailResponse from(User user, String cfDomain) {
        String profileImageUrl = null;
        if (user.getProfileImage() != null) {
            profileImageUrl = CdnUrlUtil.buildCdnUrl(user.getProfileImage().getObjectKey(), cfDomain);
        }
        return new UserDetailResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                profileImageUrl,
                user.getSeller()
        );
    }
}