package com.example.cinema.dto.theater;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅 메시지 요청 DTO
 * - 클라이언트가 보내는 메시지만 포함
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatRequest {
    private String message;
}
