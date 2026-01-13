package com.example.cinema.dto.user;

import com.example.cinema.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserGetResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Boolean seller;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserGetResponse from(User user) {
        return UserGetResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage() != null ? user.getProfileImage().getObjectKey() : null)
                .seller(user.getSeller())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
