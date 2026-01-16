package com.example.cinema.exception;

/**
 * 회원가입 시 이미 등록된 이메일로 가입을 시도할 때 발생하는 예외입니다.
 */
public class EmailDuplicateException extends BusinessException {

    public EmailDuplicateException(String message) {
        super(message, ErrorCode.EMAIL_DUPLICATION);
    }
}
