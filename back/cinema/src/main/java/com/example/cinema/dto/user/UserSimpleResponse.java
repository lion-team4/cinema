package com.example.cinema.dto.user;

import com.example.cinema.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSimpleResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;

    public static UserSimpleResponse from(User user) {
        return UserSimpleResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage() != null ? user.getProfileImage().getObjectKey() : null)
                .build();
    }
}
