package com.example.cinema.exception;

/**
 * 이미 유효한 구독을 보유하고 있는 사용자가 중복 구독을 시도할 때 발생하는 예외입니다.
 */
public class SubscriptionAlreadyExistsException extends BusinessException {

    public SubscriptionAlreadyExistsException(String message) {
        super(message, ErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
    }
}
