package com.example.cinema.dto.theater;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private Long scheduleId;
    private String message;
    private String nickname;  // 이메일 대신 닉네임 사용
    private LocalDateTime sentAt;

    public static ChatResponse from(Long scheduleId, String message, String nickname) {
        return ChatResponse.builder()
                .scheduleId(scheduleId)
                .message(message)
                .nickname(nickname)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
