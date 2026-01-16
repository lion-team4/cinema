package com.example.cinema.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {
        @GetMapping("/test/exception/business")
        public void throwBusinessException() {
            throw new BusinessException("테스트 예외입니다.", ErrorCode.USER_NOT_FOUND);
        }

        @GetMapping("/test/exception/illegal-argument")
        public void throwIllegalArgumentException() {
            throw new IllegalArgumentException("잘못된 입력입니다.");
        }

        @GetMapping("/test/exception/illegal-state")
        public void throwIllegalStateException() {
            throw new IllegalStateException("서버 상태 오류입니다.");
        }

        @GetMapping("/test/exception/access-denied")
        public void throwAccessDeniedException() {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        @GetMapping("/test/exception/generic")
        public void throwGenericException() throws Exception {
            throw new Exception("예상치 못한 서버 오류.");
        }
    }

    @Test
    @DisplayName("BusinessException 발생 시 상태 코드와 메시지가 일치해야 한다")
    void handleBusinessExceptionTest() throws Exception {
        mockMvc.perform(get("/test/exception/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("테스트 예외입니다."));
    }

    @Test
    @DisplayName("IllegalArgumentException 발생 시 400 에러를 반환한다")
    void handleIllegalArgumentExceptionTest() throws Exception {
        mockMvc.perform(get("/test/exception/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    @DisplayName("IllegalStateException 발생 시 409 Conflict 에러를 반환한다")
    void handleIllegalStateExceptionTest() throws Exception {
        mockMvc.perform(get("/test/exception/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("요청을 처리할 수 없는 상태입니다."));
    }

    @Test
    @DisplayName("AccessDeniedException 발생 시 403 Forbidden 에러를 반환한다")
    void handleAccessDeniedExceptionTest() throws Exception {
        mockMvc.perform(get("/test/exception/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP Method로 요청 시 405 Method Not Allowed 에러를 반환한다")
    void handleHttpRequestMethodNotSupportedExceptionTest() throws Exception {
        // GET만 정의된 곳에 POST 요청
        mockMvc.perform(post("/test/exception/business"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value(ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("처리되지 않은 일반 Exception 발생 시 500 에러를 반환한다")
    void handleGenericExceptionTest() throws Exception {
        mockMvc.perform(get("/test/exception/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
