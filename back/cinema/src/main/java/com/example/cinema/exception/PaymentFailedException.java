package com.example.cinema.exception;

/**
 * PG사 결제 승인 과정에서 잔액 부족, 카드 정지 등의 사유로 결제가 실패했을 때 발생하는 예외입니다.
 */
public class PaymentFailedException extends BusinessException {

    public PaymentFailedException(String message) {
        super(message, ErrorCode.PAYMENT_FAILED);
    }
}
