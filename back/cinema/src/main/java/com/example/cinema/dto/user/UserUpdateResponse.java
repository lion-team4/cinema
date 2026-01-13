package com.example.cinema.dto.user;

import com.example.cinema.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateResponse {
    private Long userId;
    private String nickname;
    private String email;

    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }
}
