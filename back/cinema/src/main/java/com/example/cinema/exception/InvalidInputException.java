package com.example.cinema.exception;

/**
 * 클라이언트의 입력값이 비즈니스 로직상 유효하지 않을 때 발생하는 예외입니다.
 * (@Valid 검증 실패 외에 로직상 허용되지 않는 값 등)
 */
public class InvalidInputException extends BusinessException {

    public InvalidInputException(String message) {
        super(message, ErrorCode.INVALID_INPUT_VALUE);
    }
}
