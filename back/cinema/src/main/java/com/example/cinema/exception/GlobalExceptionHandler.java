package com.example.cinema.exception;

import com.example.cinema.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다.
     * HttpMessageConverter 에서 등록한 HttpMessageMapper binding 못할경우 발생
     * 주로 @RequestBody, @RequestPart 어노테이션에서 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(ApiResponse.error(errorMessage), ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    /**
     * @ModelAttribute 으로 binding error 발생시 BindException 발생한다.
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.error("handleBindException", e);
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(ApiResponse.error(errorMessage), ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    /**
     * 지원하지 않는 HTTP method 호출시 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        return new ResponseEntity<>(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED.getMessage()), ErrorCode.METHOD_NOT_ALLOWED.getStatus());
    }

    /**
     * Authentication 객체가 필요한 권한을 보유하지 않은 경우 발생
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("handleAccessDeniedException", e);
        return new ResponseEntity<>(ApiResponse.error(ErrorCode.ACCESS_DENIED.getMessage()), ErrorCode.ACCESS_DENIED.getStatus());
    }

    /**
     * [기존 통합] 잘못된 인자 전달 시 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException", e);
        return new ResponseEntity<>(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getMessage()), ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    /**
     * [기존 통합] 객체 상태가 메서드 호출에 적절하지 않을 때 (Conflict)
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        log.error("handleIllegalStateException", e);
        return new ResponseEntity<>(ApiResponse.error("요청을 처리할 수 없는 상태입니다."), HttpStatus.CONFLICT);
    }

    /**
     * 비즈니스 로직 실행 중 발생한 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(final BusinessException e) {
        log.error("handleBusinessException", e);
        final ErrorCode errorCode = e.getErrorCode();
        return new ResponseEntity<>(ApiResponse.error(e.getMessage()), errorCode.getStatus());
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("handleException", e);
        return new ResponseEntity<>(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()), ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}