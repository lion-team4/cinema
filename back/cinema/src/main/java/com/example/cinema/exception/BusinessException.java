package com.example.cinema.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 실행 중 발생하는 모든 예외의 최상위 클래스입니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
