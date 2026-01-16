package com.example.cinema.exception;

/**
 * 로그인 시 비밀번호가 일치하지 않거나, 보안상 비밀번호 검증에 실패했을 때 발생하는 예외입니다.
 */
public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException(String message) {
        super(message, ErrorCode.LOGIN_FAILED);
    }
}
