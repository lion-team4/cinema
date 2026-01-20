package com.example.cinema.exception;

public class TagDuplicateException extends BusinessException {

    public TagDuplicateException() {
        super(ErrorCode.TAG_DUPLICATION);
    }

    public TagDuplicateException(String message) {
        super(message, ErrorCode.TAG_DUPLICATION);
    }
}
