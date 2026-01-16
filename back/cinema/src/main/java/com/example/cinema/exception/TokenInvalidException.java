package com.example.cinema.exception;

/**
 * JWT 토큰이 만료되었거나, 서명이 잘못되었거나, 지원하지 않는 형식일 때 발생하는 예외입니다.
 */
public class TokenInvalidException extends BusinessException {

    public TokenInvalidException(String message) {
        super(message, ErrorCode.INVALID_TOKEN);
    }
}
