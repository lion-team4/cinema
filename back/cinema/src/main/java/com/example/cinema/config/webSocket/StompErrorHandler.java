package com.example.cinema.config.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        log.error("STOMP Error: {}", ex.getMessage());

        // 인증 관련 에러인 경우 ERROR 프레임 전송
        if (ex.getCause() instanceof IllegalArgumentException) {
            return createErrorMessage(ex.getCause().getMessage());
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private Message<byte[]> createErrorMessage(String errorMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorMessage);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                errorMessage.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }
}
