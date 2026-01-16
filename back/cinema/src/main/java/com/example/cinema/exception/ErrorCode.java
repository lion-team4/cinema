package com.example.cinema.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "데이터를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

    // User
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "U001", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "사용자를 찾을 수 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "U003", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U004", "유효하지 않은 토큰입니다."),
    NICKNAME_DUPLICATION(HttpStatus.BAD_REQUEST, "U005", "이미 사용 중인 닉네임입니다."),

    // Subscription & Payment
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "S001", "이미 이용 중인 구독이 존재합니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제 승인에 실패했습니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "등록된 결제 수단이 없습니다."),

    // Content & Schedule
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "콘텐츠를 찾을 수 없습니다."),
    THEATER_NOT_FOUND(HttpStatus.NOT_FOUND, "SC001", "상영관을 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SC002", "상영 일정을 찾을 수 없습니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "SC003", "상영 일정이 겹칩니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
