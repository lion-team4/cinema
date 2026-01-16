package com.example.cinema.config.webSocket;

import com.example.cinema.config.common.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            log.warn("StompHeaderAccessor is null");
            return message;
        }

        StompCommand command = accessor.getCommand();
        log.debug("STOMP Command: {}", command);

        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = firstNativeHeader(accessor, "Authorization");
            log.debug("Authorization header: {}", authHeader != null ? "present" : "missing");
            
            String token = extractBearerToken(authHeader);
            
            if (token == null) {
                log.warn("WebSocket CONNECT rejected: Missing or invalid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
            
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket CONNECT rejected: Invalid JWT token");
                throw new IllegalArgumentException("Invalid JWT token");
            }

            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                accessor.setUser(authentication);
                log.info("WebSocket CONNECT authenticated: {}", authentication.getName());
            } catch (Exception e) {
                log.error("WebSocket CONNECT failed to get authentication: {}", e.getMessage());
                throw new IllegalArgumentException("Failed to authenticate: " + e.getMessage());
            }
        }

        return message;
    }

    private static String firstNativeHeader(StompHeaderAccessor accessor, String key) {
        List<String> values = accessor.getNativeHeader(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        
        String trimmed = authorizationHeader.trim();
        if (!trimmed.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        
        String token = trimmed.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}
