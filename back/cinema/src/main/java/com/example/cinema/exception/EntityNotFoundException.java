package com.example.cinema.exception;

/**
 * 요청한 리소스(엔티티)를 찾을 수 없을 때 발생하는 예외입니다.
 * (예: 존재하지 않는 사용자 ID, 삭제된 게시글 등)
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String message) {
        super(message, ErrorCode.ENTITY_NOT_FOUND);
    }
    
    public EntityNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
