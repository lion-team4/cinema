package com.example.cinema.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorRes> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(new ErrorRes("CONFLICT", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRes> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorRes("BAD_REQUEST", e.getMessage()));
    }

    public record ErrorRes(String code, String message) {
    }
}
